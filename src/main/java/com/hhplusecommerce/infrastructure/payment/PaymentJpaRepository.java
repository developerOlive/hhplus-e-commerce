package com.hhplusecommerce.infrastructure.payment;

import com.hhplusecommerce.domain.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentJpaRepository extends JpaRepository<Payment, Long> {
}
