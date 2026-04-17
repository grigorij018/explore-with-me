package ru.practicum.ewm.stats.server.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.stats.dto.DateTimeFormat;

import java.time.LocalDateTime;

@Value
@Builder
public class ApiError {
    String status;
    String reason;
    String message;

    @JsonFormat(pattern = DateTimeFormat.PATTERN)
    LocalDateTime timestamp;
}
