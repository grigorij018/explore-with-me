package ru.practicum.ewm.main.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import ru.practicum.ewm.stats.dto.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ApiError {
    List<String> errors;
    String message;
    String reason;
    String status;
    @JsonFormat(pattern = DateTimeFormat.PATTERN)
    LocalDateTime timestamp;
}
