package com.hhplusecommerce.interfaces.order;

public final class OrderSwaggerDocs {

    public static final String ORDER_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "orderId": 1001,
            "totalAmount": 25000,
            "finalAmount": 20000,
            "status": "PENDING"
          }
        }
    """;

    public static final String INSUFFICIENT_BALANCE = """
        {
          "success": false,
          "message": "잔액이 부족합니다.",
          "data": null
        }
    """;

    public static final String COUPON_INACTIVE = """
        {
          "success": false,
          "message": "유효하지 않은 쿠폰입니다.",
          "data": null
        }
    """;

    public static final String INSUFFICIENT_STOCK = """
        {
          "success": false,
          "message": "상품 재고가 부족합니다.",
          "data": null
        }
    """;

    private OrderSwaggerDocs() {}
}
