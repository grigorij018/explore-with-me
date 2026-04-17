package ru.practicum.ewm.stats.server.hit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.server.stats.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("""
            select h.app as app, h.uri as uri, count(h.id) as hits
            from EndpointHit h
            where h.timestamp between :start and :end
            group by h.app, h.uri
            order by hits desc
            """)
    List<ViewStatsProjection> findStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select h.app as app, h.uri as uri, count(h.id) as hits
            from EndpointHit h
            where h.timestamp between :start and :end
              and h.uri in :uris
            group by h.app, h.uri
            order by hits desc
            """)
    List<ViewStatsProjection> findStatsByUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") Collection<String> uris
    );

    @Query("""
            select h.app as app, h.uri as uri, count(distinct h.ip) as hits
            from EndpointHit h
            where h.timestamp between :start and :end
            group by h.app, h.uri
            order by hits desc
            """)
    List<ViewStatsProjection> findUniqueStats(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            select h.app as app, h.uri as uri, count(distinct h.ip) as hits
            from EndpointHit h
            where h.timestamp between :start and :end
              and h.uri in :uris
            group by h.app, h.uri
            order by hits desc
            """)
    List<ViewStatsProjection> findUniqueStatsByUris(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("uris") Collection<String> uris
    );
}
