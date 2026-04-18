package ru.practicum.ewm.main.event;

import ru.practicum.ewm.main.category.CategoryMapper;
import ru.practicum.ewm.main.dto.event.EventFullDto;
import ru.practicum.ewm.main.dto.event.EventShortDto;
import ru.practicum.ewm.main.dto.event.Location;
import ru.practicum.ewm.main.dto.event.NewEventDto;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserMapper;

import java.time.LocalDateTime;

public final class EventMapper {
    private EventMapper() {
    }

    public static Event toEntity(NewEventDto dto, User initiator, ru.practicum.ewm.main.category.Category category) {
        return Event.builder()
                .annotation(dto.getAnnotation())
                .category(category)
                .description(dto.getDescription())
                .eventDate(dto.getEventDate())
                .initiator(initiator)
                .location(toEmbeddable(dto.getLocation()))
                .paid(Boolean.TRUE.equals(dto.getPaid()))
                .participantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration() == null || dto.getRequestModeration())
                .state(ru.practicum.ewm.main.dto.event.EventState.PENDING)
                .title(dto.getTitle())
                .createdOn(LocalDateTime.now())
                .build();
    }

    public static EventShortDto toShortDto(Event event, long confirmedRequests, long views) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventFullDto toFullDto(Event event, long confirmedRequests, long views) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toDto(event.getCategory()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .id(event.getId())
                .initiator(UserMapper.toShortDto(event.getInitiator()))
                .location(toDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static LocationEmbeddable toEmbeddable(Location location) {
        return LocationEmbeddable.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }

    private static Location toDto(LocationEmbeddable location) {
        return Location.builder()
                .lat(location.getLat())
                .lon(location.getLon())
                .build();
    }
}
