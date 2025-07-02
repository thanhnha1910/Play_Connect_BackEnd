package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.FieldType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldTypeRepository extends JpaRepository<FieldType, Long> {
}