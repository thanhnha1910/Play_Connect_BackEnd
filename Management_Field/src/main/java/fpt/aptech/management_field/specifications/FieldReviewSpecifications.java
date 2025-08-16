package fpt.aptech.management_field.specifications;

import fpt.aptech.management_field.models.FieldReview;
import fpt.aptech.management_field.models.Field;
import fpt.aptech.management_field.models.Location;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FieldReviewSpecifications {

    public static Specification<FieldReview> byLocationId(Long locationId) {
        return (root, query, cb) -> {
            if (locationId == null) return cb.conjunction();
            Join<FieldReview, Field> field = root.join("field");
            Join<Field, Location> location = field.join("location");
            return cb.equal(location.get("locationId"), locationId);
        };
    }

    public static Specification<FieldReview> byFieldId(Long fieldId) {
        return (root, query, cb) -> {
            if (fieldId == null) return cb.conjunction();
            return cb.equal(root.get("field").get("fieldId"), fieldId);
        };
    }

    public static Specification<FieldReview> ratingBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return cb.conjunction();
            if (min != null && max != null) return cb.between(root.get("rating"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("rating"), min);
            return cb.lessThanOrEqualTo(root.get("rating"), max);
        };
    }

    public static Specification<FieldReview> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from != null && to != null) return cb.between(root.get("createdAt"), from, to);
            if (from != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            return cb.lessThanOrEqualTo(root.get("createdAt"), to);
        };
    }

    public static Specification<FieldReview> keyword(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("comment")), like);
        };
    }
}
