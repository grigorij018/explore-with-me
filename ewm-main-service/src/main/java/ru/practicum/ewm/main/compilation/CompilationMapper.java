package ru.practicum.ewm.main.compilation;

import lombok.experimental.UtilityClass;
import ru.practicum.ewm.main.dto.compilation.CompilationDto;
import ru.practicum.ewm.main.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.main.dto.event.EventShortDto;
import ru.practicum.ewm.main.event.Event;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

@UtilityClass
public class CompilationMapper {
    public static Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        return Compilation.builder()
                .events(events)
                .pinned(Boolean.TRUE.equals(dto.getPinned()))
                .title(dto.getTitle())
                .build();
    }

    public static CompilationDto toDto(Compilation compilation, Collection<EventShortDto> events) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(new LinkedHashSet<>(events))
                .build();
    }
}
