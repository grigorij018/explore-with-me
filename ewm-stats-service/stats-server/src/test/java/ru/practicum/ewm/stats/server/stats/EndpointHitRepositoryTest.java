package ru.practicum.ewm.stats.server.stats;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.ewm.stats.server.hit.EndpointHit;
import ru.practicum.ewm.stats.server.hit.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EndpointHitRepositoryTest {
    @Autowired
    private EndpointHitRepository repository;

    @Test
    void shouldCountUniqueHitsByIpAndFilterUris() {
        LocalDateTime timestamp = LocalDateTime.of(2022, 9, 6, 11, 0, 0);
        repository.saveAll(List.of(
                hit("/events/1", "192.168.0.1", timestamp),
                hit("/events/1", "192.168.0.1", timestamp.plusMinutes(1)),
                hit("/events/1", "192.168.0.2", timestamp.plusMinutes(2)),
                hit("/events/2", "192.168.0.3", timestamp.plusMinutes(3))
        ));

        List<ViewStatsProjection> stats = repository.findUniqueStatsByUris(
                timestamp.minusHours(1),
                timestamp.plusHours(1),
                List.of("/events/1")
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().getUri()).isEqualTo("/events/1");
        assertThat(stats.getFirst().getHits()).isEqualTo(2);
    }

    private static EndpointHit hit(String uri, String ip, LocalDateTime timestamp) {
        return EndpointHit.builder()
                .app("ewm-main-service")
                .uri(uri)
                .ip(ip)
                .timestamp(timestamp)
                .build();
    }
}
