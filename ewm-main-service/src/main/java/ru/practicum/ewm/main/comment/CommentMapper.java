package ru.practicum.ewm.main.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.dto.comment.CommentDto;

@UtilityClass
public class CommentMapper {
    public static CommentDto toDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .authorName(comment.getAuthor().getName())
                .eventId(comment.getEvent().getId())
                .status(comment.getStatus())
                .createdOn(comment.getCreatedOn())
                .updatedOn(comment.getUpdatedOn())
                .build();
    }
}
