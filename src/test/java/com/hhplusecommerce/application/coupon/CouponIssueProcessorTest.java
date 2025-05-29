package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponIssueProcessorTest {

    @Mock CouponIssuePort couponIssuePort;
    @Mock CouponService couponService;

    @InjectMocks
    CouponIssueProcessor processor;

    private static final Long TEST_COUPON_ID = 1L;
    private static final int TEST_BATCH_SIZE = 5;
    private static final String REQUEST_KEY = "coupon:request:" + TEST_COUPON_ID;
    private static final String ISSUED_KEY = "coupon:issued:" + TEST_COUPON_ID;
    private static final String STOCK_KEY = "coupon:stock:" + TEST_COUPON_ID;


    @BeforeEach
    void setup() {
        // CouponIssuePort의 getXXXKey 메서드 Mocking 추가
        lenient().when(couponIssuePort.getRequestQueueKey(TEST_COUPON_ID)).thenReturn(REQUEST_KEY);
        lenient().when(couponIssuePort.getIssuedKey(TEST_COUPON_ID)).thenReturn(ISSUED_KEY);
        lenient().when(couponIssuePort.getStockKey(TEST_COUPON_ID)).thenReturn(STOCK_KEY);
    }

    @Test
    void 이미_발급완료된_쿠폰은_추가발급_처리하지_않는다() {
        // given
        Coupon finishedCoupon = mock(Coupon.class);
        when(finishedCoupon.getIssueStatus()).thenReturn(CouponIssueStatus.FINISHED);
        when(couponService.getCoupon(TEST_COUPON_ID)).thenReturn(finishedCoupon);

        // when
        processor.processCouponIssues(TEST_COUPON_ID, TEST_BATCH_SIZE);

        // then
        verify(couponIssuePort, never()).popRequests(anyString(), anyInt());
    }

    @Test
    void 발급_요청이_없으면_즉시_처리_종료한다() {
        // given
        Coupon processingCoupon = mock(Coupon.class);
        when(processingCoupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(TEST_COUPON_ID)).thenReturn(processingCoupon);
        when(couponIssuePort.popRequests(REQUEST_KEY, TEST_BATCH_SIZE)).thenReturn(Set.of());

        // when
        processor.processCouponIssues(TEST_COUPON_ID, TEST_BATCH_SIZE);

        // then
        verify(couponIssuePort, never()).isIssued(anyString(), anyString());
    }

    @Test
    void 쿠폰_요청_사용자_목록을_정상적으로_처리한다() {
        // given
        Coupon processingCoupon = mock(Coupon.class);
        when(processingCoupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(TEST_COUPON_ID)).thenReturn(processingCoupon);

        Set<String> users = Set.of("10", "20");
        when(couponIssuePort.popRequests(REQUEST_KEY, TEST_BATCH_SIZE)).thenReturn(users);

        when(couponIssuePort.isIssued(ISSUED_KEY, "10")).thenReturn(false);
        when(couponIssuePort.isIssued(ISSUED_KEY, "20")).thenReturn(false);

        when(couponIssuePort.decrementStock(STOCK_KEY)).thenReturn(2L, 1L);

        doNothing().when(couponIssuePort).incrementStock(anyString());
        doNothing().when(couponIssuePort).addIssuedUser(anyString(), anyString());
        doNothing().when(couponIssuePort).removeIssuedUser(anyString(), anyString());

        // when
        processor.processCouponIssues(TEST_COUPON_ID, TEST_BATCH_SIZE);

        // then
        verify(couponIssuePort, times(2)).isIssued(eq(ISSUED_KEY), anyString());
        verify(couponIssuePort, times(2)).decrementStock(eq(STOCK_KEY));
        verify(couponIssuePort, times(2)).addIssuedUser(eq(ISSUED_KEY), anyString());
        verify(couponService, never()).finishCoupon(eq(TEST_COUPON_ID));
        verify(couponService, times(2)).issueCoupon(any(CouponCommand.class));
    }

    @Test
    void 재고가_소진되면_쿠폰_발급을_종료한다() {
        // given
        Coupon processingCoupon = mock(Coupon.class);
        when(processingCoupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(TEST_COUPON_ID)).thenReturn(processingCoupon);

        Set<String> users = Set.of("10");
        when(couponIssuePort.popRequests(REQUEST_KEY, TEST_BATCH_SIZE)).thenReturn(users);

        when(couponIssuePort.isIssued(ISSUED_KEY, "10")).thenReturn(false);
        when(couponIssuePort.decrementStock(STOCK_KEY)).thenReturn(0L);

        doNothing().when(couponIssuePort).addIssuedUser(ISSUED_KEY, "10");

        // when
        processor.processCouponIssues(TEST_COUPON_ID, TEST_BATCH_SIZE);

        // then
        verify(couponService).finishCoupon(TEST_COUPON_ID);
        verify(couponService).issueCoupon(any(CouponCommand.class));
    }

    @Test
    void 쿠폰_발급_처리_중_오류가_발생하면_발급_정보를_롤백한다() {
        // given
        Coupon processingCoupon = mock(Coupon.class);
        when(processingCoupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(TEST_COUPON_ID)).thenReturn(processingCoupon);

        Set<String> users = Set.of("10");
        when(couponIssuePort.popRequests(REQUEST_KEY, TEST_BATCH_SIZE)).thenReturn(users);

        when(couponIssuePort.isIssued(ISSUED_KEY, "10")).thenReturn(false);
        when(couponIssuePort.decrementStock(STOCK_KEY)).thenReturn(1L);

        doNothing().when(couponIssuePort).addIssuedUser(ISSUED_KEY, "10");

        doThrow(new RuntimeException("DB 오류")).when(couponService).issueCoupon(any(CouponCommand.class));
        doNothing().when(couponIssuePort).removeIssuedUser(ISSUED_KEY, "10");
        doNothing().when(couponIssuePort).incrementStock(STOCK_KEY);

        // when & then (예외 발생 확인)
        assertThrows(RuntimeException.class, () -> {
            processor.processCouponIssues(TEST_COUPON_ID, TEST_BATCH_SIZE);
        });

        // then (롤백 메서드 호출 확인)
        verify(couponIssuePort).removeIssuedUser(ISSUED_KEY, "10");
        verify(couponIssuePort).incrementStock(STOCK_KEY);
        verify(couponService, never()).finishCoupon(anyLong());
    }
}
