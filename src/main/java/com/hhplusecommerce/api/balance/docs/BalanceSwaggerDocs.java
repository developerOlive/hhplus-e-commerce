package com.hhplusecommerce.api.balance.docs;

public final class BalanceSwaggerDocs {

    // 잔액 조회 성공
    public static final String BALANCE_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "amount": 10000
          }
        }
    """;

    // 잔액 조회 - 사용자 없음
    public static final String BALANCE_NOT_FOUND = """
        {
          "success": false,
          "message": "존재하지 않는 사용자입니다.",
          "data": null
        }
    """;

    // 잔액 충전 성공
    public static final String BALANCE_CHARGE_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "amount": 15000
          }
        }
    """;

    // 잔액 충전 - 금액 필수
    public static final String BALANCE_CHARGE_AMOUNT_REQUIRED = """
        {
          "success": false,
          "message": "충전 금액은 필수입니다.",
          "data": null
        }
    """;

    // 잔액 충전 - 음수 불가
    public static final String BALANCE_CHARGE_AMOUNT_POSITIVE = """
        {
          "success": false,
          "message": "충전 금액은 0보다 커야 합니다.",
          "data": null
        }
    """;

    // 잔액 충전 - 사용자 없음
    public static final String BALANCE_CHARGE_USER_NOT_FOUND = """
        {
          "success": false,
          "message": "존재하지 않는 사용자입니다.",
          "data": null
        }
    """;

    // 잔액 충전 - 예외 상황 (서버 오류 등)
    public static final String BALANCE_CHARGE_FAILED = """
        {
          "success": false,
          "message": "충전이 실패했습니다.",
          "data": null
        }
    """;

    private BalanceSwaggerDocs() {}
}
