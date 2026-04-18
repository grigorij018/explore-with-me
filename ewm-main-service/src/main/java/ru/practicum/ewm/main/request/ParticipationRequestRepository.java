package ru.practicum.ewm.main.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.dto.request.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByIdInAndEventId(Collection<Long> ids, Long eventId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    @Query("""
            select r.event.id as eventId, count(r.id) as count
            from ParticipationRequest r
            where r.event.id in :eventIds and r.status = :status
            group by r.event.id
            """)
    List<ConfirmedCountProjection> countByEventIdsAndStatus(
            @Param("eventIds") Collection<Long> eventIds,
            @Param("status") RequestStatus status
    );

    interface ConfirmedCountProjection {
        Long getEventId();

        Long getCount();
    }
}
