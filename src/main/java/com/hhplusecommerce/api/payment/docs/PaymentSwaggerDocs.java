package com.hhplusecommerce.api.payment.docs;

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

    public static final String PAYMENT_FAILED = """
        {
          "success": false,
          "message": "결제에 실패했습니다.",
          "data": null
        }
    """;

    private PaymentSwaggerDocs() {}
}
