package com.hhplusecommerce.application.order;

import com.hhplusecommerce.domain.order.OrderStatus;

import java.math.BigDecimal;

public record OrderResult(Long orderId,
                          BigDecimal totalAmount,
                          BigDecimal finalAmount,
                          OrderStatus orderStatus) {
}
