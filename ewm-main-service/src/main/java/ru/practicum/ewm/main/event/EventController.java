package ru.practicum.ewm.main.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.main.common.DateTimeParser;
import ru.practicum.ewm.main.dto.event.EventFullDto;
import ru.practicum.ewm.main.dto.event.EventShortDto;
import ru.practicum.ewm.main.dto.event.EventSort;
import ru.practicum.ewm.main.dto.event.EventState;
import ru.practicum.ewm.main.dto.event.NewEventDto;
import ru.practicum.ewm.main.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.main.dto.event.UpdateEventUserRequest;
import ru.practicum.ewm.main.stats.StatsAdapter;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final StatsAdapter statsAdapter;

    @GetMapping("/admin/events")
    public List<EventFullDto> adminSearch(@RequestParam(required = false) List<Long> users,
                                          @RequestParam(required = false) List<EventState> states,
                                          @RequestParam(required = false) List<Long> categories,
                                          @RequestParam(required = false) String rangeStart,
                                          @RequestParam(required = false) String rangeEnd,
                                          @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                          @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.adminSearch(users, states, categories,
                DateTimeParser.parseNullable(rangeStart, "rangeStart"),
                DateTimeParser.parseNullable(rangeEnd, "rangeEnd"),
                from, size);
    }

    @PatchMapping("/admin/events/{eventId}")
    public EventFullDto updateAdminEvent(@PathVariable Long eventId,
                                         @Valid @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateAdminEvent(eventId, request);
    }

    @GetMapping("/events")
    public List<EventShortDto> publicSearch(@RequestParam(required = false) String text,
                                            @RequestParam(required = false) List<Long> categories,
                                            @RequestParam(required = false) Boolean paid,
                                            @RequestParam(required = false) String rangeStart,
                                            @RequestParam(required = false) String rangeEnd,
                                            @RequestParam(defaultValue = "false") boolean onlyAvailable,
                                            @RequestParam(required = false) EventSort sort,
                                            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                            @RequestParam(defaultValue = "10") @Positive int size,
                                            HttpServletRequest servletRequest) {
        statsAdapter.hit(servletRequest.getRequestURI(), servletRequest.getRemoteAddr());
        return eventService.publicSearch(text, categories, paid,
                DateTimeParser.parseNullable(rangeStart, "rangeStart"),
                DateTimeParser.parseNullable(rangeEnd, "rangeEnd"),
                onlyAvailable, sort, from, size);
    }

    @GetMapping("/events/{id}")
    public EventFullDto publicGet(@PathVariable Long id, HttpServletRequest servletRequest) {
        statsAdapter.hit(servletRequest.getRequestURI(), servletRequest.getRemoteAddr());
        return eventService.publicGet(id);
    }

    @GetMapping("/users/{userId}/events")
    public List<EventShortDto> getUserEvents(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") @PositiveOrZero int from,
                                             @RequestParam(defaultValue = "10") @Positive int size) {
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable Long userId, @Valid @RequestBody NewEventDto request) {
        return eventService.create(userId, request);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public EventFullDto getUserEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable Long userId,
                                        @PathVariable Long eventId,
                                        @Valid @RequestBody UpdateEventUserRequest request) {
        return eventService.updateUserEvent(userId, eventId, request);
    }
}
