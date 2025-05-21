package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.port.CouponIssuePort;
import com.hhplusecommerce.domain.coupon.service.CouponService;
import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponIssueFacadeTest {

    private static final Long COUPON_ID = 101L;
    private static final Long USER_ID = 202L;

    @Mock
    private CouponIssueProcessor couponIssueProcessor;

    @Mock
    private CouponIssuePort couponIssuePort;

    @Mock
    private CouponService couponService;

    @Mock
    private CouponKeyProvider couponKeyProvider;

    @InjectMocks
    private CouponIssueFacade couponIssueFacade;

    @Nested
    class RequestCouponIssueTest {
        private static final String ISSUED_KEY = "coupon:issued:" + COUPON_ID;
        private static final String REQUEST_KEY = "coupon:request:" + COUPON_ID;

        @Test
        void 이미_발급된_쿠폰_요청시_예외가_발생한다() {
            // given
            when(couponKeyProvider.issuedKey(COUPON_ID)).thenReturn(ISSUED_KEY);
            when(couponIssuePort.isIssued(ISSUED_KEY, USER_ID.toString())).thenReturn(true);

            // when + then
            CustomException ex = assertThrows(CustomException.class,
                    () -> couponIssueFacade.requestCouponIssue(new CouponCommand(USER_ID, COUPON_ID)));

            assertEquals(ErrorType.COUPON_ALREADY_ISSUED, ex.getErrorType());
            verify(couponIssuePort, never()).addToRequestQueue(any(), any(), anyLong());
        }

        @Test
        void 정상_요청이면_대기열에_추가한다() {
            // given
            when(couponKeyProvider.issuedKey(COUPON_ID)).thenReturn(ISSUED_KEY);
            when(couponIssuePort.isIssued(ISSUED_KEY, USER_ID.toString())).thenReturn(false);
            when(couponKeyProvider.requestKey(COUPON_ID)).thenReturn(REQUEST_KEY);

            // when
            couponIssueFacade.requestCouponIssue(new CouponCommand(USER_ID, COUPON_ID));

            // then
            verify(couponIssuePort).addToRequestQueue(eq(REQUEST_KEY), eq(USER_ID.toString()), anyLong());
        }
    }
}
