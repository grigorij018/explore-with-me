package ru.practicum.ewm.stats.server.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.exception.BadRequestException;
import ru.practicum.ewm.stats.server.hit.EndpointHitMapper;
import ru.practicum.ewm.stats.server.hit.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private final EndpointHitRepository hitRepository;

    @Transactional
    public void saveHit(EndpointHitDto endpointHit) {
        hitRepository.save(EndpointHitMapper.toEntity(endpointHit));
    }

    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Start date must not be after end date");
        }

        List<ViewStatsProjection> stats;
        if (uris == null || uris.isEmpty()) {
            stats = unique ? hitRepository.findUniqueStats(start, end) : hitRepository.findStats(start, end);
        } else {
            stats = unique
                    ? hitRepository.findUniqueStatsByUris(start, end, uris)
                    : hitRepository.findStatsByUris(start, end, uris);
        }

        return stats.stream()
                .map(stat -> ViewStatsDto.builder()
                        .app(stat.getApp())
                        .uri(stat.getUri())
                        .hits(stat.getHits())
                        .build())
                .toList();
    }
}
