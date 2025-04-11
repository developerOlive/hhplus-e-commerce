package com.hhplusecommerce.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long paymentId);

    Payment save(Payment payment);
}
