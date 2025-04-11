package com.hhplusecommerce.interfaces.balance;

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

    // 잔액 조회 불가
    public static final String USER_BALANCE_NOT_FOUND = """
        {
          "success": false,
          "message": "사용자의 잔액 정보가 존재하지 않습니다.",
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

    public static final String INVALID_BALANCE_AMOUNT = """
        {
          "success": false,
          "message": "잔액은 0보다 커야 합니다.",
          "data": null
        }
    """;

    // 잔액 차감 성공
    public static final String BALANCE_DEDUCT_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "amount": 5000
          }
        }
    """;

    public static final String INSUFFICIENT_BALANCE = """
        {
          "success": false,
          "message": "잔액이 부족합니다.",
          "data": {
            "amount": 5000
          }
        }
    """;

    private BalanceSwaggerDocs() {}
}
