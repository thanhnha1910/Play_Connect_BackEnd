package fpt.aptech.management_field.services;

import fpt.aptech.management_field.exception.ResourceNotFoundException;
import fpt.aptech.management_field.models.FieldReview;
import fpt.aptech.management_field.payload.dtos.FieldReviewListItemDTO;
import fpt.aptech.management_field.repositories.FieldReviewRepository;
import fpt.aptech.management_field.specifications.FieldReviewSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final FieldReviewRepository fieldReviewRepository;

    public Page<FieldReviewListItemDTO> searchFieldReviews(
            Long locationId,
            Long fieldId,
            BigDecimal minRating,
            BigDecimal maxRating,
            LocalDate fromDate,
            LocalDate toDate,
            String q,
            String sortBy,
            Sort.Direction dir,
            int page,
            int size
    ) {
        Specification<FieldReview> spec = Specification.allOf(
                FieldReviewSpecifications.byLocationId(locationId),
                FieldReviewSpecifications.byFieldId(fieldId),
                FieldReviewSpecifications.ratingBetween(minRating, maxRating),
                FieldReviewSpecifications.createdBetween(
                        fromDate == null ? null : fromDate.atStartOfDay(),
                        toDate == null ? null : toDate.atTime(23, 59, 59)
                ),
                FieldReviewSpecifications.keyword(q)
        );

        String sortField = switch (sortBy == null ? "" : sortBy) {
            case "rating" -> "rating";
            case "createdAt" -> "createdAt";
            default -> "createdAt";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir == null ? Sort.Direction.DESC : dir, sortField));

        Page<FieldReview> pageData = fieldReviewRepository.findAll(spec, pageable);

        return pageData.map(fr -> {
            FieldReviewListItemDTO dto = new FieldReviewListItemDTO();
            dto.setId(fr.getFieldReviewId());
            dto.setRating(fr.getRating());
            dto.setCreatedAt(fr.getCreatedAt());
            dto.setContent(fr.getComment());

            if (fr.getUser() != null) {
                dto.setUserId(fr.getUser().getId());
                dto.setUserName(fr.getUser().getFullName());
            } else {
                dto.setUserName("Ẩn danh");
            }

            if (fr.getField() != null) {
                dto.setFieldId(fr.getField().getFieldId());
                dto.setFieldName(fr.getField().getName());
                if (fr.getField().getLocation() != null) {
                    dto.setLocationId(fr.getField().getLocation().getLocationId());
                    dto.setLocationName(fr.getField().getLocation().getName());
                }
            }

            dto.setOwnerReply(fr.getOwnerReply());
            dto.setOwnerRepliedAt(fr.getOwnerRepliedAt());

            return dto;
        });
    }

    public FieldReviewListItemDTO ownerReply(Long reviewId, String content) {
        FieldReview fr = fieldReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found: " + reviewId));

        fr.setOwnerReply(content);
        fr.setOwnerRepliedAt(LocalDateTime.now());

        fr = fieldReviewRepository.save(fr);

        FieldReviewListItemDTO dto = new FieldReviewListItemDTO();
        dto.setId(fr.getFieldReviewId());
        dto.setRating(fr.getRating());
        dto.setCreatedAt(fr.getCreatedAt());
        dto.setContent(fr.getComment());
        if (fr.getUser() != null) {
            dto.setUserId(fr.getUser().getId());
            dto.setUserName(fr.getUser().getFullName());
        } else {
            dto.setUserName("Ẩn danh");
        }
        if (fr.getField() != null) {
            dto.setFieldId(fr.getField().getFieldId());
            dto.setFieldName(fr.getField().getName());
            if (fr.getField().getLocation() != null) {
                dto.setLocationId(fr.getField().getLocation().getLocationId());
                dto.setLocationName(fr.getField().getLocation().getName());
            }
        }
        dto.setOwnerReply(fr.getOwnerReply());
        dto.setOwnerRepliedAt(fr.getOwnerRepliedAt());
        return dto;
    }
}
