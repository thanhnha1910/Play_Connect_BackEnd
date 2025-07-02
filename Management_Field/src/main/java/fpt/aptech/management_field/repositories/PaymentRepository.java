package fpt.aptech.management_field.repositories;

import fpt.aptech.management_field.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}