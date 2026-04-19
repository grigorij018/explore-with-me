package ru.practicum.ewm.main.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.event.EventState;
import ru.practicum.ewm.main.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.main.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.main.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.main.dto.request.RequestStatus;
import ru.practicum.ewm.main.error.ConflictException;
import ru.practicum.ewm.main.error.NotFoundException;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventService;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipationRequestService {
    private final ParticipationRequestRepository requestRepository;
    private final UserService userService;
    private final EventService eventService;

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        userService.getExisting(userId);
        return requestRepository.findByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = userService.getExisting(userId);
        Event event = eventService.getExisting(eventId);
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Initiator cannot request participation in own event");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot participate in unpublished event");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Participation request already exists");
        }
        long confirmed = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmed >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }
        RequestStatus status = event.getParticipantLimit() == 0 || !event.getRequestModeration()
                ? RequestStatus.CONFIRMED
                : RequestStatus.PENDING;
        ParticipationRequest request = ParticipationRequest.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();
        return ParticipationRequestMapper.toDto(requestRepository.save(request));
    }

    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        userService.getExisting(userId);
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        request.setStatus(RequestStatus.CANCELED);
        return ParticipationRequestMapper.toDto(request);
    }

    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        Event event = eventService.getExisting(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        return requestRepository.findByEventId(eventId).stream()
                .map(ParticipationRequestMapper::toDto)
                .toList();
    }

    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest update) {
        Event event = eventService.getExisting(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        List<ParticipationRequest> requests = requestRepository.findByIdInAndEventId(update.getRequestIds(), eventId);
        for (ParticipationRequest request : requests) {
            if (request.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }
        }

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();
        if (update.getStatus() == RequestStatus.REJECTED) {
            requests.forEach(request -> {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(ParticipationRequestMapper.toDto(request));
            });
            return EventRequestStatusUpdateResult.builder()
                    .confirmedRequests(confirmed)
                    .rejectedRequests(rejected)
                    .build();
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("The participant limit has been reached");
        }
        for (ParticipationRequest request : requests) {
            if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
                request.setStatus(RequestStatus.REJECTED);
                rejected.add(ParticipationRequestMapper.toDto(request));
            } else {
                request.setStatus(RequestStatus.CONFIRMED);
                confirmedCount++;
                confirmed.add(ParticipationRequestMapper.toDto(request));
            }
        }
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            requestRepository.findByEventId(eventId).stream()
                    .filter(request -> request.getStatus() == RequestStatus.PENDING)
                    .forEach(request -> request.setStatus(RequestStatus.REJECTED));
        }
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmed)
                .rejectedRequests(rejected)
                .build();
    }
}
