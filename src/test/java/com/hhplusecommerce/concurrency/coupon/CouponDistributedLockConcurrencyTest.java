package com.hhplusecommerce.concurrency.coupon;

import com.hhplusecommerce.concurrency.ConcurrencyResult;
import com.hhplusecommerce.domain.coupon.*;
import com.hhplusecommerce.support.ConcurrencyTestSupport;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class CouponDistributedLockConcurrencyTest extends ConcurrencyTestSupport {

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Nested
    class 쿠폰_분산락_동시성_테스트 {

        private static final int INITIAL_ISSUED_QUANTITY = 8;
        private static final int MAX_COUPON_QUANTITY = 10;
        private static final int CONCURRENT_USERS = 5;

        private long couponId;

        @BeforeEach
        void setUpCoupon() {
            Coupon coupon = Coupon.builder()
                    .couponName("분산락 테스트 쿠폰")
                    .discountType(CouponDiscountType.FIXED_AMOUNT)
                    .discountValue(BigDecimal.valueOf(10))
                    .maxQuantity(MAX_COUPON_QUANTITY)
                    .issuedQuantity(INITIAL_ISSUED_QUANTITY)
                    .validStartDate(LocalDate.now())
                    .validEndDate(LocalDate.now().plusDays(10))
                    .couponType(CouponType.LIMITED)
                    .build();
            couponId = couponRepository.save(coupon).getId();
        }

        @Test
        void issueCoupon_10개수량_8개발급상태_5명요청시_2명만발급성공() throws InterruptedException {
            int expectedSuccess = MAX_COUPON_QUANTITY - INITIAL_ISSUED_QUANTITY;

            ConcurrencyResult result = executeWithLatch(CONCURRENT_USERS, r -> {
                long userId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
                log.info("🟡 [{}] 쿠폰 발급 시도", userId);
                try {
                    Long issuedId = couponService.issueCoupon(new CouponCommand(userId, couponId));
                    log.info("🟢 [{}] 쿠폰 발급 성공 - couponHistoryId: {}", userId, issuedId);
                    r.success();
                } catch (Exception e) {
                    log.error("🔴 [{}] 쿠폰 발급 실패: {}", userId, e.getMessage(), e);
                    r.error();
                }
            });

            Coupon updated = couponRepository.findById(couponId).orElseThrow();
            int finalIssued = updated.getIssuedQuantity();
            int actualSuccess = result.getSuccessCount();
            int actualFailure = result.getErrorCount();

            log.warn("🔒 [분산락 쿠폰 발급 테스트 결과]");
            log.warn("▶ 초기 발급 수량: {}, 최대 수량: {}", INITIAL_ISSUED_QUANTITY, MAX_COUPON_QUANTITY);
            log.warn("▶ 동시 요청 수: {}", CONCURRENT_USERS);
            log.warn("▶ 최종 발급 수량: {}", finalIssued);
            log.warn("▶ 성공 수: {}, 실패 수: {}", actualSuccess, actualFailure);

            assertEquals(INITIAL_ISSUED_QUANTITY + actualSuccess, finalIssued);
            assertEquals(MAX_COUPON_QUANTITY, finalIssued);
            assertEquals(CONCURRENT_USERS, actualSuccess + actualFailure);
            assertTrue(actualSuccess <= expectedSuccess);
        }
    }
}
