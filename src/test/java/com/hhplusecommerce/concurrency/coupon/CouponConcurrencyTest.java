package com.hhplusecommerce.concurrency.coupon;

import com.hhplusecommerce.support.ConcurrencyTestSupport;
import com.hhplusecommerce.concurrency.ConcurrencyResult;
import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.repository.CouponRepository;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@Slf4j
class CouponConcurrencyTest extends ConcurrencyTestSupport {

    @Autowired
    CouponRepository couponRepository;

    @Autowired
    CouponService couponService;

    @Nested
    class 시나리오1_잔여수량_내_동시_요청 {

        private static final int ALREADY_ISSUED = 490;
        private static final int MAX = 500;
        private static final int REQUEST_COUNT = 10;
        private Coupon coupon;

        @BeforeEach
        void setUp() {
            coupon = couponRepository.save(Instancio.of(Coupon.class)
                    .set(field("couponName"), "잔여수량동시요청")
                    .set(field("maxQuantity"), MAX)
                    .set(field("issuedQuantity"), ALREADY_ISSUED)
                    .create());
        }

        @Test
        void 동시에_쿠폰_발급_요청_시_남은_쿠폰수량_만큼만_성공해야_한다() throws InterruptedException {
            ConcurrencyResult result = executeWithLatch(REQUEST_COUNT, r -> {
                long userId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
                log.info("🟡 [{}] 쿠폰 발급 시도", userId);
                try {
                    Long historyId = couponService.issueCoupon(new CouponCommand(userId, coupon.getId()));
                    log.info("🟢 [{}] 쿠폰 발급 성공 - historyId: {}", userId, historyId);
                    r.success();
                } catch (Exception e) {
                    log.error("🔴 [{}] 쿠폰 발급 실패: {}", userId, e.getMessage());
                    r.error();
                }
            });

            Coupon updated = couponRepository.findById(coupon.getId()).orElseThrow();

            log.warn("🟢 [잔여수량 내 동시 요청 테스트 결과]");
            log.warn("\u25B6 최초 발급 수량: {}, 최대 수량: {}", ALREADY_ISSUED, MAX);
            log.warn("\u25B6 동시 요청 수: {}", REQUEST_COUNT);
            log.warn("\u25B6 최종 발급 수량: {}", updated.getIssuedQuantity());
            log.warn("\u25B6 성공 수: {}, 실패 수: {}", result.getSuccessCount(), result.getErrorCount());

            assertThat(updated.getIssuedQuantity()).isEqualTo(MAX);
            assertThat(result.getSuccessCount()).isEqualTo(MAX - ALREADY_ISSUED);
            assertThat(result.getErrorCount()).isEqualTo(REQUEST_COUNT - (MAX - ALREADY_ISSUED));
        }
    }

    @Nested
    class 시나리오2_잔여수량_없는_경우 {

        private static final int MAX = 500;
        private static final int ISSUED = 500;
        private static final int THREAD_COUNT = 5;
        private Coupon coupon;

        @BeforeEach
        void setUp() {
            String uniqueName = "전량발급_" + System.nanoTime();
            coupon = couponRepository.save(Instancio.of(Coupon.class)
                    .set(field("couponName"), uniqueName)
                    .set(field("maxQuantity"), MAX)
                    .set(field("issuedQuantity"), ISSUED)
                    .create());
        }

        @Test
        void 전량발급_완료된_쿠폰에_동시요청시_모두_실패해야_한다() throws InterruptedException {
            ConcurrencyResult result = executeWithLatch(THREAD_COUNT, (r) -> {
                long userId = ThreadLocalRandom.current().nextLong(1, 1_000_000);
                log.info("🟡 [{}] 쿠폰 발급 시도", userId);
                try {
                    couponService.issueCoupon(new CouponCommand(userId, coupon.getId()));
                    log.info("🟢 [{}] 쿠폰 발급 성공", userId);
                    r.success();
                } catch (Exception e) {
                    log.error("🔴 [{}] 쿠폰 발급 실패: {}", userId, e.getMessage());
                    r.error();
                }
            });

            Coupon updated = couponRepository.findById(coupon.getId()).orElseThrow();

            assertThat(updated.getIssuedQuantity()).isEqualTo(MAX);
            assertThat(result.getSuccessCount()).isEqualTo(0);
            assertThat(result.getErrorCount()).isEqualTo(THREAD_COUNT);
        }
    }
}
