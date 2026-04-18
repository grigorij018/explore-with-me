package ru.practicum.ewm.main.request;

import ru.practicum.ewm.main.dto.request.ParticipationRequestDto;

public final class ParticipationRequestMapper {
    private ParticipationRequestMapper() {
    }

    public static ParticipationRequestDto toDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .created(request.getCreated())
                .event(request.getEvent().getId())
                .id(request.getId())
                .requester(request.getRequester().getId())
                .status(request.getStatus())
                .build();
    }
}
