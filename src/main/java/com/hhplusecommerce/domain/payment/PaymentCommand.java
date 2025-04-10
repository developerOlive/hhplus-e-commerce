package com.hhplusecommerce.domain.payment;

public record PaymentCommand(
        Long orderId,
        PaymentMethod paymentMethod
) {}
