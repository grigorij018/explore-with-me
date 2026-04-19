package ru.practicum.ewm.main.stats;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsAdapter {
    private final StatsClient statsClient;

    public void hit(String uri, String ip) {
        try {
            statsClient.hit(EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException exception) {
            log.warn("Failed to send hit to stats-service for uri={}", uri, exception);
        }
    }

    public Map<String, Long> getViews(Map<String, LocalDateTime> publishedOnByUri) {
        if (publishedOnByUri == null || publishedOnByUri.isEmpty()) {
            return Map.of();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = resolveStatsStart(publishedOnByUri, now);
        try {
            return statsClient.getStats(start, now, List.copyOf(publishedOnByUri.keySet()), true).stream()
                    .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits, Long::sum));
        } catch (RuntimeException exception) {
            log.warn("Failed to get views from stats-service for uris={}", publishedOnByUri.keySet(), exception);
            return Map.of();
        }
    }

    private LocalDateTime resolveStatsStart(Map<String, LocalDateTime> publishedOnByUri, LocalDateTime now) {
        if (publishedOnByUri.size() == 1) {
            LocalDateTime publishedOn = publishedOnByUri.values().iterator().next();
            return publishedOn == null ? now.minusYears(1) : publishedOn;
        }
        return now.minusYears(1);
    }
}
