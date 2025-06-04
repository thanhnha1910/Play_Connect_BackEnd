package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.FieldCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldCategoryRepository extends JpaRepository<FieldCategory, Long> {
}