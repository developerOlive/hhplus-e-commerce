package com.hhplusecommerce.infrastructure.cache;

import com.hhplusecommerce.domain.popularProduct.PopularProductCommand;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * baseDate 기준 해당 날짜의 마지막 순간(23:59:59)까지 TTL을 설정
 * 자정이 지나거나 baseDate가 과거일 경우 TTL 1시간으로 설정
 */
public class EndOfDayTtlStrategy implements TtlStrategy {
    @Override
    public Duration computeTTL(Object keyParams) {
        if (!(keyParams instanceof PopularProductCommand command)) {
            throw new IllegalArgumentException(
                    String.format("Invalid keyParams type: expected %s but got %s",
                            PopularProductCommand.class.getSimpleName(),
                            keyParams == null ? "null" : keyParams.getClass().getSimpleName())
            );
        }

        LocalDate baseDate = command.baseDate() != null ? command.baseDate() : LocalDate.now();
        LocalDateTime endOfDay = baseDate.atTime(23, 59, 59);
        Duration duration = Duration.between(LocalDateTime.now(), endOfDay);

        if (duration.isNegative()) {
            return Duration.ofHours(1);
        }

        return duration;
    }
}
