package ru.practicum.ewm.main.event;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.main.dto.event.EventState;

import java.time.LocalDateTime;
import java.util.Collection;

@UtilityClass
public class EventSpecifications {
    public static Specification<Event> usersIn(Collection<Long> users) {
        return (root, query, cb) -> users == null || users.isEmpty()
                ? cb.conjunction()
                : root.get("initiator").get("id").in(users);
    }

    public static Specification<Event> statesIn(Collection<EventState> states) {
        return (root, query, cb) -> states == null || states.isEmpty()
                ? cb.conjunction()
                : root.get("state").in(states);
    }

    public static Specification<Event> categoriesIn(Collection<Long> categories) {
        return (root, query, cb) -> categories == null || categories.isEmpty()
                ? cb.conjunction()
                : root.get("category").get("id").in(categories);
    }

    public static Specification<Event> eventDateAfterOrEqual(LocalDateTime start) {
        return (root, query, cb) -> start == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("eventDate"), start);
    }

    public static Specification<Event> eventDateBeforeOrEqual(LocalDateTime end) {
        return (root, query, cb) -> end == null
                ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("eventDate"), end);
    }

    public static Specification<Event> textContains(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + text.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("annotation")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Event> paid(Boolean paid) {
        return (root, query, cb) -> paid == null ? cb.conjunction() : cb.equal(root.get("paid"), paid);
    }
}
