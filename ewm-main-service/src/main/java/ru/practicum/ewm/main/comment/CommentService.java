package ru.practicum.ewm.main.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.common.OffsetPageRequest;
import ru.practicum.ewm.main.dto.comment.AdminUpdateCommentRequest;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.dto.comment.CommentStatus;
import ru.practicum.ewm.main.dto.comment.NewCommentDto;
import ru.practicum.ewm.main.dto.comment.UpdateCommentDto;
import ru.practicum.ewm.main.dto.event.EventState;
import ru.practicum.ewm.main.error.ConflictException;
import ru.practicum.ewm.main.error.NotFoundException;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventService;
import ru.practicum.ewm.main.user.User;
import ru.practicum.ewm.main.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    @Transactional
    public CommentDto create(Long userId, Long eventId, NewCommentDto dto) {
        User author = userService.getExisting(userId);
        Event event = eventService.getExisting(eventId);
        validatePublished(event);
        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .text(dto.getText())
                .author(author)
                .event(event)
                .status(CommentStatus.PENDING)
                .createdOn(now)
                .updatedOn(now)
                .build();
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getUserComments(Long userId, Long eventId, int from, int size) {
        userService.getExisting(userId);
        OffsetPageRequest page = new OffsetPageRequest(from, size, Sort.by(Sort.Direction.DESC, "createdOn"));
        List<Comment> comments = eventId == null
                ? commentRepository.findByAuthorId(userId, page)
                : commentRepository.findByAuthorIdAndEventId(userId, eventId, page);
        return comments.stream().map(CommentMapper::toDto).toList();
    }

    @Transactional
    public CommentDto update(Long userId, Long commentId, UpdateCommentDto dto) {
        userService.getExisting(userId);
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        comment.setText(dto.getText());
        comment.setStatus(CommentStatus.PENDING);
        comment.setUpdatedOn(LocalDateTime.now());
        return CommentMapper.toDto(comment);
    }

    @Transactional
    public void deleteOwn(Long userId, Long commentId) {
        userService.getExisting(userId);
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
        commentRepository.delete(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> getPublicEventComments(Long eventId, int from, int size) {
        Event event = eventService.getExisting(eventId);
        validatePublished(event);
        return commentRepository.findByEventIdAndStatus(
                        eventId,
                        CommentStatus.APPROVED,
                        new OffsetPageRequest(from, size, Sort.by(Sort.Direction.DESC, "createdOn"))
                ).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentDto> adminSearch(Long eventId, Long authorId, CommentStatus status, int from, int size) {
        Specification<Comment> spec = Specification.where(CommentSpecifications.eventId(eventId))
                .and(CommentSpecifications.authorId(authorId))
                .and(CommentSpecifications.status(status));
        return commentRepository.findAll(spec, new OffsetPageRequest(from, size, Sort.by("id"))).stream()
                .map(CommentMapper::toDto)
                .toList();
    }

    @Transactional
    public CommentDto adminUpdate(Long commentId, AdminUpdateCommentRequest request) {
        Comment comment = getExisting(commentId);
        comment.setStatus(request.getStatus());
        comment.setUpdatedOn(LocalDateTime.now());
        return CommentMapper.toDto(comment);
    }

    @Transactional
    public void adminDelete(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id=" + commentId + " was not found");
        }
        commentRepository.deleteById(commentId);
    }

    private Comment getExisting(Long commentId) {
        return commentRepository.findWithAuthorAndEventById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
    }

    private void validatePublished(Event event) {
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Comments are available only for published events");
        }
    }
}
