package ru.practicum.ewm.main.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.common.OffsetPageRequest;
import ru.practicum.ewm.main.dto.compilation.CompilationDto;
import ru.practicum.ewm.main.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.main.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.main.event.Event;
import ru.practicum.ewm.main.event.EventService;
import ru.practicum.ewm.main.error.NotFoundException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventService eventService;

    @Transactional
    public CompilationDto create(NewCompilationDto dto) {
        Set<Event> events = new LinkedHashSet<>(eventService.findEvents(dto.getEvents()));
        Compilation compilation = compilationRepository.save(CompilationMapper.toEntity(dto, events));
        return toDto(compilation);
    }

    @Transactional
    public void delete(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = getExisting(compId);
        if (request.getEvents() != null) {
            compilation.setEvents(new LinkedHashSet<>(eventService.findEvents(request.getEvents())));
        }
        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }
        if (request.getTitle() != null) {
            compilation.setTitle(request.getTitle());
        }
        return toDto(compilation);
    }

    @Transactional(readOnly = true)
    public List<CompilationDto> get(Boolean pinned, int from, int size) {
        List<Compilation> compilations = pinned == null
                ? compilationRepository.findAllBy(new OffsetPageRequest(from, size))
                : compilationRepository.findByPinned(pinned, new OffsetPageRequest(from, size));
        return compilations.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CompilationDto get(Long compId) {
        return toDto(getExisting(compId));
    }

    private Compilation getExisting(Long compId) {
        return compilationRepository.findWithEventsById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private CompilationDto toDto(Compilation compilation) {
        return CompilationMapper.toDto(compilation, eventService.toShortDtos(List.copyOf(compilation.getEvents())));
    }
}
