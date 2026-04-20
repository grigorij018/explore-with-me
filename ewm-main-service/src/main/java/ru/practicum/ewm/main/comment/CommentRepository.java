package ru.practicum.ewm.main.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.main.dto.comment.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    @EntityGraph(attributePaths = {"author", "event"})
    Optional<Comment> findWithAuthorAndEventById(Long commentId);

    @EntityGraph(attributePaths = {"author", "event"})
    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    @EntityGraph(attributePaths = {"author", "event"})
    List<Comment> findByAuthorId(Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "event"})
    List<Comment> findByAuthorIdAndEventId(Long authorId, Long eventId, Pageable pageable);

    @EntityGraph(attributePaths = {"author", "event"})
    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status, Pageable pageable);
}
