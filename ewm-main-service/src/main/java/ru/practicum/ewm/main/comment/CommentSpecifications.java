package ru.practicum.ewm.main.comment;

import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.main.dto.comment.CommentStatus;

@UtilityClass
public class CommentSpecifications {
    public static Specification<Comment> eventId(Long eventId) {
        return (root, query, cb) -> eventId == null
                ? cb.conjunction()
                : cb.equal(root.get("event").get("id"), eventId);
    }

    public static Specification<Comment> authorId(Long authorId) {
        return (root, query, cb) -> authorId == null
                ? cb.conjunction()
                : cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<Comment> status(CommentStatus status) {
        return (root, query, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }
}
