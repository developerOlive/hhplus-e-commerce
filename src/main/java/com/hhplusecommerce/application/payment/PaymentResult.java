package com.hhplusecommerce.application.payment;

import com.hhplusecommerce.domain.payment.PaymentStatus;

import java.math.BigDecimal;

public record PaymentResult(
        Long paymentId,
        BigDecimal paidAmount,
        PaymentStatus paymentStatus
) {}
