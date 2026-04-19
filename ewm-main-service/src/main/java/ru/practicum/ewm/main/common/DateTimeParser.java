package ru.practicum.ewm.main.common;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.error.BadRequestException;
import ru.practicum.ewm.stats.dto.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@UtilityClass
public class DateTimeParser {
    public static LocalDateTime parseNullable(String value, String name) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormat.FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException("Parameter " + name + " must match format " + DateTimeFormat.PATTERN);
        }
    }
}
