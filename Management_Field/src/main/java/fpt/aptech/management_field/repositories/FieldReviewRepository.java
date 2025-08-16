package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.FieldReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FieldReviewRepository extends JpaRepository<FieldReview, Long>, JpaSpecificationExecutor<FieldReview> {
}
