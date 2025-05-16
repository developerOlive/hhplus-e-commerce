package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import com.hhplusecommerce.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static com.hhplusecommerce.domain.coupon.type.CouponDiscountType.FIXED_AMOUNT;
import static com.hhplusecommerce.domain.coupon.type.CouponType.LIMITED;
import static org.assertj.core.api.Assertions.assertThat;

public class CouponIssueProcessorIntegrationTest extends IntegrationTestSupport {

    private static final Logger log = LoggerFactory.getLogger(CouponIssueProcessorIntegrationTest.class);

    @Autowired
    private CouponIssueProcessor processor;

    @Autowired
    private CouponService couponService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final Long TEST_COUPON_ID = 1L;

    @BeforeEach
    void setUp() {
        try {
            couponService.getCoupon(TEST_COUPON_ID);
        } catch (Exception e) {
            Coupon coupon = Coupon.builder()
                    .couponName("통합 테스트 쿠폰")
                    .discountType(FIXED_AMOUNT)
                    .discountValue(BigDecimal.ONE)
                    .maxQuantity(10)
                    .validStartDate(LocalDate.now().minusDays(1))
                    .validEndDate(LocalDate.now().plusDays(1))
                    .issuedQuantity(0)
                    .couponType(LIMITED)
                    .build();
            couponService.saveCoupon(coupon);
            log.info("테스트용 쿠폰 생성 완료");
        }

        // Redis 키 초기화
        redisTemplate.delete("coupon:request:" + TEST_COUPON_ID);
        redisTemplate.delete("coupon:issued:" + TEST_COUPON_ID);
        redisTemplate.delete("coupon:stock:" + TEST_COUPON_ID);  // 재고 키 삭제

        // 재고 초기값 세팅 (maxQuantity 만큼)
        redisTemplate.opsForValue().set("coupon:stock:" + TEST_COUPON_ID, "10");

        Set<String> keysBefore = redisTemplate.keys("coupon:*");
        log.info("테스트 시작 전 Redis 키 목록: {}", keysBefore);
    }

    @Test
    void 쿠폰_요청_사용자_목록을_정상적으로_처리한다() throws InterruptedException {
        // given
        Coupon coupon = couponService.getCoupon(TEST_COUPON_ID);
        assertThat(coupon.getIssueStatus()).isNotEqualTo(CouponIssueStatus.FINISHED);

        String requestKey = "coupon:request:" + TEST_COUPON_ID;

        redisTemplate.opsForZSet().add(requestKey, "1", System.currentTimeMillis());
        redisTemplate.opsForZSet().add(requestKey, "2", System.currentTimeMillis());
        redisTemplate.opsForZSet().add(requestKey, "3", System.currentTimeMillis());

        log.info("배치 처리 실행 전 Redis 대기열 상태: {}", redisTemplate.opsForZSet().range(requestKey, 0, -1));

        // when
        processor.processCouponIssues(TEST_COUPON_ID, 5);

        // then
        Set<String> keysAfter = redisTemplate.keys("coupon:*");
        log.info("처리 후 Redis 키 목록: {}", keysAfter);

        Thread.sleep(500);

        String issuedKey = "coupon:issued:" + TEST_COUPON_ID;
        Set<String> issuedUsers = redisTemplate.opsForSet().members(issuedKey);
        log.info("발급된 사용자 목록: {}", issuedUsers);

        assertThat(issuedUsers).contains(
                String.valueOf(1L),
                String.valueOf(2L),
                String.valueOf(3L)
        );

        Coupon updatedCoupon = couponService.getCoupon(TEST_COUPON_ID);
        assertThat(updatedCoupon.getIssueStatus()).isNotEqualTo(CouponIssueStatus.FINISHED);
    }
}
