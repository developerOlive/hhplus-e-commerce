package com.hhplusecommerce.concurrency.coupon;

import com.hhplusecommerce.ConcurrencyTestSupport;
import com.hhplusecommerce.domain.coupon.Coupon;
import com.hhplusecommerce.domain.coupon.CouponCommand;
import com.hhplusecommerce.domain.coupon.CouponRepository;
import com.hhplusecommerce.domain.coupon.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
            List<Boolean> successFlags = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);

            executeConcurrency(
                    REQUEST_COUNT,
                    () -> {
                        try {
                            couponService.issueCoupon(new CouponCommand(ThreadLocalRandom.current().nextLong(1, 1_000_000), coupon.getId()));
                            successFlags.add(true);
                        } catch (Throwable e) {
                            successFlags.add(false);
                            errors.add(e);
                            log.warn("예외 발생: {}", e.getMessage(), e);
                        } finally {
                            latch.countDown();
                        }
                    }
            );

            latch.await();

            Coupon updated = couponRepository.findById(coupon.getId()).orElseThrow();
            long successCount = successFlags.stream().filter(Boolean::booleanValue).count();
            int failCount = (int) successFlags.stream().filter(flag -> !flag).count();

            log.warn("🟢 [잔여수량 내 동시 요청 테스트 결과]");
            log.warn("▶ 초기 발급 수량: {}, 최대 수량: {}", ALREADY_ISSUED, MAX);
            log.warn("▶ 동시 요청 수: {}", REQUEST_COUNT);
            log.warn("▶ 최종 발급 수량: {}", updated.getIssuedQuantity());
            log.warn("▶ 성공 수: {}, 실패 수: {}", successCount, failCount);

            assertThat(updated.getIssuedQuantity()).isEqualTo(MAX);
            assertThat(successCount).isEqualTo(REQUEST_COUNT);
            assertThat(failCount).isEqualTo(0);
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
            // 고유한 쿠폰 이름 사용 (중복 방지)
            String uniqueName = "전량발급_" + System.nanoTime();
            coupon = couponRepository.save(Instancio.of(Coupon.class)
                    .set(field("couponName"), uniqueName)
                    .set(field("maxQuantity"), MAX)
                    .set(field("issuedQuantity"), ISSUED)
                    .create());
        }

        @Test
        void 전량발급_완료된_쿠폰에_동시요청시_모두_실패해야한다() throws InterruptedException {
            List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
            List<Boolean> successFlags = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

            executeConcurrency(
                    THREAD_COUNT,
                    () -> {
                        try {
                            couponService.issueCoupon(new CouponCommand(1L, coupon.getId()));
                            successFlags.add(true);
                        } catch (Throwable e) {
                            successFlags.add(false);
                            errors.add(e);
                            log.warn("예외 발생: {}", e.getMessage(), e);
                        } finally {
                            latch.countDown();
                        }
                    }
            );

            latch.await();

            Coupon updated = couponRepository.findById(coupon.getId()).orElseThrow();
            long successCount = successFlags.stream().filter(Boolean::booleanValue).count();
            int failCount = (int) successFlags.stream().filter(flag -> !flag).count();

            log.warn("🔒 [전량 발급된 쿠폰 테스트 결과]");
            log.warn("▶ 초기 발급 수량: {}, 최대 수량: {}", ISSUED, MAX);
            log.warn("▶ 동시 요청 수: {}", THREAD_COUNT);
            log.warn("▶ 최종 발급 수량: {}", updated.getIssuedQuantity());
            log.warn("▶ 성공 수: {}, 실패 수: {}", successCount, failCount);

            assertThat(updated.getIssuedQuantity()).isEqualTo(MAX);
            assertThat(successCount).isEqualTo(0);
            assertThat(failCount).isEqualTo(THREAD_COUNT);
        }
    }
}
