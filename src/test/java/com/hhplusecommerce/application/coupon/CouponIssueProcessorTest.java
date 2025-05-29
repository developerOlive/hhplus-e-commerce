package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.application.coupon.event.CouponIssueCompletedEvent;
import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.model.Coupon;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.domain.coupon.type.CouponIssueStatus;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static com.hhplusecommerce.application.coupon.event.CouponIssueResult.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponIssueProcessorTest {

    @Mock
    CouponService couponService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    CouponIssueProcessor processor;

    private final Long userId = 100L;
    private final Long couponId = 1L;

    @BeforeEach
    void setUp() {
        Mockito.reset(couponService, eventPublisher);
    }

    @Test
    void 쿠폰이_이미_마감된_경우_예외가_발생한다() {
        // given
        Coupon coupon = mock(Coupon.class);
        when(coupon.getIssueStatus()).thenReturn(CouponIssueStatus.FINISHED);
        when(couponService.getCoupon(couponId)).thenReturn(coupon);

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> {
            processor.processCouponIssue(userId, couponId);
        });

        assertEquals(ErrorType.COUPON_ALREADY_FINISHED, ex.getErrorType());
        verify(couponService, never()).issueCoupon(any());

        // 발행된 이벤트의 내용 검증
        ArgumentCaptor<CouponIssueCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CouponIssueCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CouponIssueCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.userId());
        assertEquals(couponId, capturedEvent.couponId());
        assertEquals(OUT_OF_STOCK, capturedEvent.result());
    }

    @Test
    void 이미_사용자에게_쿠폰이_발급된_경우_추가처리_없이_종료된다() {
        // given
        Coupon coupon = mock(Coupon.class);
        when(coupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(couponId)).thenReturn(coupon);
        when(couponService.isCouponAlreadyIssued(userId, couponId)).thenReturn(true);

        // when
        processor.processCouponIssue(userId, couponId);

        // then
        verify(couponService, never()).issueCoupon(any());

        // 발행된 이벤트의 내용 검증
        ArgumentCaptor<CouponIssueCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CouponIssueCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CouponIssueCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.userId());
        assertEquals(couponId, capturedEvent.couponId());
        assertEquals(ALREADY_ISSUED, capturedEvent.result());
    }

    @Test
    void 정상적으로_쿠폰이_발급된다() {
        // given
        Coupon coupon = mock(Coupon.class);
        when(coupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(couponId)).thenReturn(coupon);
        when(couponService.isCouponAlreadyIssued(userId, couponId)).thenReturn(false);
        when(couponService.issueCoupon(any(CouponCommand.class))).thenReturn(1L);
        when(couponService.getCouponCurrentStock(couponId)).thenReturn(10);

        // when
        processor.processCouponIssue(userId, couponId);

        // then
        verify(couponService).issueCoupon(any(CouponCommand.class));
        verify(couponService, never()).finishCoupon(anyLong());

        // 발행된 이벤트의 내용 검증
        ArgumentCaptor<CouponIssueCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CouponIssueCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CouponIssueCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.userId());
        assertEquals(couponId, capturedEvent.couponId());
        assertEquals(SUCCESS, capturedEvent.result());
    }

    @Test
    void 발급_후_재고가_0이면_쿠폰이_마감_처리된다() {
        // given
        Coupon coupon = mock(Coupon.class);
        when(coupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(couponId)).thenReturn(coupon);
        when(couponService.isCouponAlreadyIssued(userId, couponId)).thenReturn(false);
        when(couponService.issueCoupon(any(CouponCommand.class))).thenReturn(1L);
        when(couponService.getCouponCurrentStock(couponId)).thenReturn(0);

        // when
        processor.processCouponIssue(userId, couponId);

        // then
        verify(couponService).issueCoupon(any(CouponCommand.class));
        verify(couponService).finishCoupon(couponId);

        // 발행된 이벤트의 내용 검증
        ArgumentCaptor<CouponIssueCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CouponIssueCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CouponIssueCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.userId());
        assertEquals(couponId, capturedEvent.couponId());
        assertEquals(SUCCESS, capturedEvent.result());
    }

    @Test
    void 쿠폰_발급_중_CustomException_발생_시_이벤트가_발행되고_예외가_던져진다() {
        // given
        Coupon coupon = mock(Coupon.class);
        when(coupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(couponId)).thenReturn(coupon);
        when(couponService.isCouponAlreadyIssued(userId, couponId)).thenReturn(false);
        doThrow(new CustomException(ErrorType.COUPON_NO_STOCK)).when(couponService).issueCoupon(any(CouponCommand.class));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> {
            processor.processCouponIssue(userId, couponId);
        });

        assertEquals(ErrorType.COUPON_NO_STOCK, ex.getErrorType());
        verify(couponService).issueCoupon(any(CouponCommand.class));

        // 발행된 이벤트의 내용 검증
        ArgumentCaptor<CouponIssueCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CouponIssueCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CouponIssueCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.userId());
        assertEquals(couponId, capturedEvent.couponId());
        assertEquals(FAILED_SYSTEM, capturedEvent.result());
    }

    @Test
    void 쿠폰_발급_중_일반_Exception_발생_시_이벤트가_발행되고_CustomException이_던져진다() {
        // given
        Coupon coupon = mock(Coupon.class);
        when(coupon.getIssueStatus()).thenReturn(CouponIssueStatus.PROCESSING);
        when(couponService.getCoupon(couponId)).thenReturn(coupon);
        when(couponService.isCouponAlreadyIssued(userId, couponId)).thenReturn(false);
        doThrow(new RuntimeException("DB Connection Error")).when(couponService).issueCoupon(any(CouponCommand.class));

        // when & then
        CustomException ex = assertThrows(CustomException.class, () -> {
            processor.processCouponIssue(userId, couponId);
        });

        assertEquals(ErrorType.UNKNOWN_ERROR, ex.getErrorType());
        verify(couponService).issueCoupon(any(CouponCommand.class));

        // 발행된 이벤트의 내용 검증
        ArgumentCaptor<CouponIssueCompletedEvent> eventCaptor = ArgumentCaptor.forClass(CouponIssueCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        CouponIssueCompletedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(userId, capturedEvent.userId());
        assertEquals(couponId, capturedEvent.couponId());
        assertEquals(FAILED_SYSTEM, capturedEvent.result());
    }
}
