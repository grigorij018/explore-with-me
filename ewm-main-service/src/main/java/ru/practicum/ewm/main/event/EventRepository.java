package ru.practicum.ewm.main.event;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.main.dto.event.EventState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    @EntityGraph(attributePaths = {"category", "initiator"})
    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);

    @EntityGraph(attributePaths = {"category", "initiator"})
    Optional<Event> findByIdAndState(Long eventId, EventState state);

    @EntityGraph(attributePaths = {"category", "initiator"})
    List<Event> findByIdIn(Collection<Long> ids);

    boolean existsByCategoryId(Long categoryId);
}
