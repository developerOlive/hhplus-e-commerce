package com.hhplusecommerce.concurrency.coupon;

import com.hhplusecommerce.domain.coupon.*;
import com.hhplusecommerce.support.ConcurrencyTestSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@SpringBootTest
class LockReleaseTimingTest extends ConcurrencyTestSupport {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Test
    void 락해제는_트랜잭션_커밋_이후에_실행되어야_한다() {
        Coupon coupon = Coupon.builder()
                .couponName("테스트쿠폰")
                .discountType(CouponDiscountType.FIXED_AMOUNT)
                .discountValue(BigDecimal.valueOf(1000))
                .maxQuantity(10)
                .issuedQuantity(0)
                .validStartDate(LocalDate.now().minusDays(1))
                .validEndDate(LocalDate.now().plusDays(1))
                .couponType(CouponType.LIMITED)
                .build();

        Long couponId = couponRepository.save(coupon).getId();

        log.info("🟡 [TEST] 테스트 시작");

        couponService.issueCoupon(new CouponCommand(couponId, 123L));

        log.info("🔵 [TEST] 테스트 종료 (이 시점은 트랜잭션 커밋 이후)");
    }
}
