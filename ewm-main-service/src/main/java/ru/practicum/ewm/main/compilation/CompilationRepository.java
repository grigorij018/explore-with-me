package ru.practicum.ewm.main.compilation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    List<Compilation> findAllBy(Pageable pageable);

    @EntityGraph(attributePaths = {"events", "events.category", "events.initiator"})
    Optional<Compilation> findWithEventsById(Long id);
}
