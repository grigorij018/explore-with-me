package ru.practicum.ewm.main.comment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.main.dto.comment.AdminUpdateCommentRequest;
import ru.practicum.ewm.main.dto.comment.CommentDto;
import ru.practicum.ewm.main.dto.comment.CommentStatus;
import ru.practicum.ewm.main.dto.comment.NewCommentDto;
import ru.practicum.ewm.main.dto.comment.UpdateCommentDto;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto create(@PathVariable Long userId,
                             @PathVariable Long eventId,
                             @Valid @RequestBody NewCommentDto request) {
        return commentService.create(userId, eventId, request);
    }

    @GetMapping("/users/{userId}/comments")
    public List<CommentDto> getUserComments(@PathVariable Long userId,
                                            @RequestParam(required = false) Long eventId,
                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getUserComments(userId, eventId, from, size);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public CommentDto update(@PathVariable Long userId,
                             @PathVariable Long commentId,
                             @Valid @RequestBody UpdateCommentDto request) {
        return commentService.update(userId, commentId, request);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOwn(@PathVariable Long userId, @PathVariable Long commentId) {
        commentService.deleteOwn(userId, commentId);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getPublicEventComments(@PathVariable Long eventId,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                                   @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.getPublicEventComments(eventId, from, size);
    }

    @GetMapping("/admin/comments")
    public List<CommentDto> adminSearch(@RequestParam(required = false) Long eventId,
                                        @RequestParam(required = false) Long authorId,
                                        @RequestParam(required = false) CommentStatus status,
                                        @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                        @RequestParam(defaultValue = "10") @Positive int size) {
        return commentService.adminSearch(eventId, authorId, status, from, size);
    }

    @PatchMapping("/admin/comments/{commentId}")
    public CommentDto adminUpdate(@PathVariable Long commentId,
                                  @Valid @RequestBody AdminUpdateCommentRequest request) {
        return commentService.adminUpdate(commentId, request);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adminDelete(@PathVariable Long commentId) {
        commentService.adminDelete(commentId);
    }
}
