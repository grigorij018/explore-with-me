package ru.practicum.ewm.main.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StatsAdapter {
    private static final LocalDateTime STATS_START = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    private final StatsClient statsClient;

    public void hit(String uri, String ip) {
        try {
            statsClient.hit(EndpointHitDto.builder()
                    .app("ewm-main-service")
                    .uri(uri)
                    .ip(ip)
                    .timestamp(LocalDateTime.now())
                    .build());
        } catch (RuntimeException ignored) {
            // Public API must remain available if stats-service is temporarily unavailable.
        }
    }

    public Map<String, Long> getViews(Collection<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return Map.of();
        }

        try {
            return statsClient.getStats(STATS_START, LocalDateTime.now().plusYears(100), List.copyOf(uris), true).stream()
                    .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits, Long::sum));
        } catch (RuntimeException ignored) {
            return Map.of();
        }
    }
}
