package com.hhplusecommerce.api.coupon.docs;

public final class CouponSwaggerDocs {

    public static final String COUPON_LIST_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": [
            {
              "couponId": 1,
              "couponName": "10% 할인 쿠폰",
              "discountType": "FIXED_RATE",
              "discountValue": 10,
              "status": "AVAILABLE",
              "validStartDate": "2025-04-01",
              "validEndDate": "2025-04-30"
            },
            {
              "couponId": 2,
              "couponName": "5천원 할인 쿠폰",
              "discountType": "FIXED_AMOUNT",
              "discountValue": 5000,
              "status": "USED",
              "validStartDate": "2025-03-01",
              "validEndDate": "2025-03-31"
            }
          ]
        }
    """;

    public static final String COUPON_ISSUE_FAIL_NOT_FOUND = """
        {
          "success": false,
          "message": "요청한 쿠폰 또는 사용자를 찾을 수 없습니다.",
          "data": null
        }
        """;

    public static final String COUPON_ISSUE_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "couponIssueId": 100
          }
        }
    """;

    public static final String COUPON_ISSUE_FAIL_NO_STOCK = """
        {
          "success": false,
          "message": "쿠폰이 모두 소진되었습니다.",
          "data": null
        }
    """;

    public static final String COUPON_ISSUE_FAIL_ALREADY = """
        {
          "success": false,
          "message": "이미 발급받은 쿠폰입니다.",
          "data": null
        }
    """;

    private CouponSwaggerDocs() {}
}
