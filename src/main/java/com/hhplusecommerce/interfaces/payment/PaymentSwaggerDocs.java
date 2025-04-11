package com.hhplusecommerce.interfaces.payment;

public final class PaymentSwaggerDocs {

    public static final String PAYMENT_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "paymentId": 5001,
            "payAmount": 20000,
            "status": "SUCCESS"
          }
        }
    """;

    public static final String ORDER_NOT_FOUND = """
        {
          "success": false,
          "message": "해당 주문을 찾을 수 없습니다.",
          "data": null
        }
    """;

    public static final String INSUFFICIENT_BALANCE = """
        {
          "success": false,
          "message": "잔액이 부족합니다.",
          "data": null
        }
    """;

    public static final String COUPON_ALREADY_USED = """
        {
          "success": false,
          "message": "이미 사용된 쿠폰입니다.",
          "data": null
        }
    """;

    private PaymentSwaggerDocs() {}
}
