package com.hhplusecommerce.application.coupon;

import com.hhplusecommerce.domain.coupon.command.CouponCommand;
import com.hhplusecommerce.domain.coupon.port.in.CouponRequestAcceptor;
import com.hhplusecommerce.domain.coupon.port.out.CouponIssuePublisher;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponIssueFacadeTest {

    private static final Long COUPON_ID = 101L;
    private static final Long USER_ID = 202L;

    @Mock
    private CouponRequestAcceptor couponRequestAcceptor;
    @Mock
    private CouponIssuePublisher couponIssuePublisher;

    @InjectMocks
    private CouponIssueFacade couponIssueFacade;

    @Nested
    class RequestCouponIssueTest {

        @Test
        void 이미_발급된_쿠폰_요청시_예외가_발생한다() {
            // given
            doThrow(new CustomException(ErrorType.COUPON_ALREADY_ISSUED))
                    .when(couponRequestAcceptor).acceptCouponRequest(any(CouponCommand.class));

            // when + then
            CustomException ex = assertThrows(CustomException.class,
                    () -> couponIssueFacade.requestCouponIssue(new CouponCommand(USER_ID, COUPON_ID)));

            assertEquals(ErrorType.COUPON_ALREADY_ISSUED, ex.getErrorType());
            verify(couponRequestAcceptor).acceptCouponRequest(any(CouponCommand.class));
            verifyNoInteractions(couponIssuePublisher);
        }

        @Test
        void 정상_요청이면_대기열에_추가하고_카프카에_발행한다() {
            // given
            doNothing().when(couponRequestAcceptor).acceptCouponRequest(any(CouponCommand.class));
            doNothing().when(couponIssuePublisher).publishCouponRequest(any(CouponCommand.class));

            // when
            couponIssueFacade.requestCouponIssue(new CouponCommand(USER_ID, COUPON_ID));

            // then
            verify(couponRequestAcceptor).acceptCouponRequest(any(CouponCommand.class));
            verify(couponIssuePublisher).publishCouponRequest(any(CouponCommand.class));
        }
    }
}
