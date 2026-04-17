package ru.practicum.ewm.stats.server.hit;

import ru.practicum.ewm.stats.dto.EndpointHitDto;

public final class EndpointHitMapper {
    private EndpointHitMapper() {
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }
}
