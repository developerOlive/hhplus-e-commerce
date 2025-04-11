package com.hhplusecommerce.domain.order;

import java.util.List;

public record OrderCommand(
        Long userId,
        Long couponIssueId,
        List<OrderItemCommand> orderItems
) {}
