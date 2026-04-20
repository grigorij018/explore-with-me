package ru.practicum.ewm.main.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.stats.dto.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private Long authorId;
    private String authorName;
    private Long eventId;
    private CommentStatus status;
    @JsonFormat(pattern = DateTimeFormat.PATTERN)
    private LocalDateTime createdOn;
    @JsonFormat(pattern = DateTimeFormat.PATTERN)
    private LocalDateTime updatedOn;
}
