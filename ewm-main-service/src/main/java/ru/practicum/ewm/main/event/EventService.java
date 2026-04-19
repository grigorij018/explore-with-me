package ru.practicum.ewm.main.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.category.Category;
import ru.practicum.ewm.main.category.CategoryService;
import ru.practicum.ewm.main.common.OffsetPageRequest;
import ru.practicum.ewm.main.dto.event.AdminStateAction;
import ru.practicum.ewm.main.dto.event.EventFullDto;
import ru.practicum.ewm.main.dto.event.EventShortDto;
import ru.practicum.ewm.main.dto.event.EventSort;
import ru.practicum.ewm.main.dto.event.EventState;
import ru.practicum.ewm.main.dto.event.NewEventDto;
import ru.practicum.ewm.main.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.main.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.main.dto.event.UserStateAction;
import ru.practicum.ewm.main.dto.request.RequestStatus;
import ru.practicum.ewm.main.error.BadRequestException;
import ru.practicum.ewm.main.error.ConflictException;
import ru.practicum.ewm.main.error.NotFoundException;
import ru.practicum.ewm.main.request.ParticipationRequestRepository;
import ru.practicum.ewm.main.stats.StatsAdapter;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final ParticipationRequestRepository requestRepository;
    private final StatsAdapter statsAdapter;

    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least two hours from now");
        }
        User user = userService.getExisting(userId);
        Category category = categoryService.getExisting(dto.getCategory());
        Event event = eventRepository.save(EventMapper.toEntity(dto, user, category));
        return toFullDto(event);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getUserEvents(Long userId, int from, int size) {
        userService.getExisting(userId);
        return toShortDtos(eventRepository.findByInitiatorId(userId, new OffsetPageRequest(from, size)));
    }

    @Transactional(readOnly = true)
    public EventFullDto getUserEvent(Long userId, Long eventId) {
        userService.getExisting(userId);
        return toFullDto(getUserEventEntity(userId, eventId));
    }

    @Transactional
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest request) {
        userService.getExisting(userId);
        Event event = getUserEventEntity(userId, eventId);
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least two hours from now");
        }
        applyCommonUpdate(event, request.getAnnotation(), request.getCategory(), request.getDescription(),
                request.getEventDate(), request.getLocation(), request.getPaid(), request.getParticipantLimit(),
                request.getRequestModeration(), request.getTitle());
        if (request.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        } else if (request.getStateAction() == UserStateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }
        return toFullDto(event);
    }

    @Transactional(readOnly = true)
    public List<EventFullDto> adminSearch(List<Long> users, List<EventState> states, List<Long> categories,
                                          LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        validateRange(rangeStart, rangeEnd);
        Specification<Event> spec = Specification.where(EventSpecifications.usersIn(users))
                .and(EventSpecifications.statesIn(states))
                .and(EventSpecifications.categoriesIn(categories))
                .and(EventSpecifications.eventDateAfterOrEqual(rangeStart))
                .and(EventSpecifications.eventDateBeforeOrEqual(rangeEnd));
        return toFullDtos(eventRepository.findAll(spec, new OffsetPageRequest(from, size, Sort.by("id"))).getContent());
    }

    @Transactional
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest request) {
        Event event = getExisting(eventId);
        if (request.getEventDate() != null && request.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new BadRequestException("Event date must be at least one hour from now");
        }
        applyCommonUpdate(event, request.getAnnotation(), request.getCategory(), request.getDescription(),
                request.getEventDate(), request.getLocation(), request.getPaid(), request.getParticipantLimit(),
                request.getRequestModeration(), request.getTitle());
        if (request.getStateAction() == AdminStateAction.PUBLISH_EVENT) {
            if (event.getState() != EventState.PENDING) {
                throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
            }
            if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                throw new ConflictException("Event date must be at least one hour from publication");
            }
            event.setState(EventState.PUBLISHED);
            event.setPublishedOn(LocalDateTime.now());
        } else if (request.getStateAction() == AdminStateAction.REJECT_EVENT) {
            if (event.getState() == EventState.PUBLISHED) {
                throw new ConflictException("Cannot reject the event because it's already published");
            }
            event.setState(EventState.CANCELED);
        }
        return toFullDto(event);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> publicSearch(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd, boolean onlyAvailable, EventSort sort,
                                            int from, int size, String uri, String ip) {
        statsAdapter.hit(uri, ip);
        validateRange(rangeStart, rangeEnd);
        LocalDateTime start = rangeStart == null && rangeEnd == null ? LocalDateTime.now() : rangeStart;
        Specification<Event> spec = Specification.where(EventSpecifications.statesIn(List.of(EventState.PUBLISHED)))
                .and(EventSpecifications.textContains(text))
                .and(EventSpecifications.categoriesIn(categories))
                .and(EventSpecifications.paid(paid))
                .and(EventSpecifications.eventDateAfterOrEqual(start))
                .and(EventSpecifications.eventDateBeforeOrEqual(rangeEnd));

        List<Event> events = sort == EventSort.VIEWS
                ? eventRepository.findAll(spec)
                : eventRepository.findAll(spec, new OffsetPageRequest(from, size, Sort.by("eventDate"))).getContent();

        List<EventShortDto> result = toShortDtos(events);
        if (onlyAvailable) {
            result = result.stream()
                    .filter(dto -> {
                        Event event = events.stream().filter(e -> e.getId().equals(dto.getId())).findFirst().orElseThrow();
                        return event.getParticipantLimit() == 0 || dto.getConfirmedRequests() < event.getParticipantLimit();
                    })
                    .toList();
        }
        if (sort == EventSort.VIEWS) {
            return result.stream()
                    .sorted(Comparator.comparing(EventShortDto::getViews).reversed())
                    .skip(from)
                    .limit(size)
                    .toList();
        }
        return result;
    }

    @Transactional(readOnly = true)
    public EventFullDto publicGet(Long eventId, String uri, String ip) {
        statsAdapter.hit(uri, ip);
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        return toFullDto(event);
    }

    public Event getExisting(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    public List<Event> findEvents(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return eventRepository.findByIdIn(ids);
    }

    private Map<Long, Long> getConfirmedCounts(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }
        return requestRepository.countByEventIdsAndStatus(
                        events.stream().map(Event::getId).toList(),
                        RequestStatus.CONFIRMED
                ).stream()
                .collect(Collectors.toMap(
                        ParticipationRequestRepository.ConfirmedCountProjection::getEventId,
                        ParticipationRequestRepository.ConfirmedCountProjection::getCount
                ));
    }

    private Map<Long, Long> getViews(Collection<Event> events) {
        if (events == null || events.isEmpty()) {
            return Map.of();
        }
        Map<String, LocalDateTime> publishedOnByUri = new LinkedHashMap<>();
        events.forEach(event -> publishedOnByUri.put("/events/" + event.getId(), event.getPublishedOn()));
        Map<String, Long> viewsByUri = statsAdapter.getViews(publishedOnByUri);
        return events.stream()
                .collect(Collectors.toMap(Event::getId, event -> viewsByUri.getOrDefault("/events/" + event.getId(), 0L)));
    }

    private EventFullDto toFullDto(Event event) {
        return toFullDtos(List.of(event)).getFirst();
    }

    private List<EventFullDto> toFullDtos(List<Event> events) {
        Map<Long, Long> confirmed = getConfirmedCounts(events);
        Map<Long, Long> views = getViews(events);
        return events.stream()
                .map(event -> EventMapper.toFullDto(event,
                        confirmed.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    public List<EventShortDto> toShortDtos(List<Event> events) {
        Map<Long, Long> confirmed = getConfirmedCounts(events);
        Map<Long, Long> views = getViews(events);
        return events.stream()
                .map(event -> EventMapper.toShortDto(event,
                        confirmed.getOrDefault(event.getId(), 0L),
                        views.getOrDefault(event.getId(), 0L)))
                .toList();
    }

    private Event getUserEventEntity(Long userId, Long eventId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void applyCommonUpdate(Event event, String annotation, Long categoryId, String description,
                                   LocalDateTime eventDate, ru.practicum.ewm.main.dto.event.Location location,
                                   Boolean paid, Integer participantLimit, Boolean requestModeration, String title) {
        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (categoryId != null) {
            event.setCategory(categoryService.getExisting(categoryId));
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (eventDate != null) {
            event.setEventDate(eventDate);
        }
        if (location != null) {
            event.setLocation(EventMapper.toEmbeddable(location));
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (title != null) {
            event.setTitle(title);
        }
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BadRequestException("Range start must not be after range end");
        }
    }
}
