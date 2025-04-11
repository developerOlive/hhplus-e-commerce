package com.hhplusecommerce.interfaces.product;

public final class ProductSwaggerDocs {

    public static final String PRODUCT_LIST_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": [
            {
              "id": 1,
              "name": "MacBook Pro",
              "price": 2390000,
              "inventory": 12
            },
            {
              "id": 2,
              "name": "AirPods Pro",
              "price": 359000,
              "inventory": 35
            }
          ]
        }
        """;

    public static final String POPULAR_PRODUCT_SUCCESS = """
    {
      "success": true,
      "message": "요청이 성공적으로 처리되었습니다.",
      "data": [
        {
          "productId": 1,
          "name": "MacBook Pro",
          "price": 2390000,
          "totalSold": 200
        },
        {
          "productId": 2,
          "name": "AirPods Pro",
          "price": 359000,
          "totalSold": 180
        },
        {
          "productId": 3,
          "name": "iPhone 15",
          "price": 1350000,
          "totalSold": 150
        },
        {
          "productId": 4,
          "name": "iPad Pro",
          "price": 1190000,
          "totalSold": 120
        },
        {
          "productId": 5,
          "name": "Apple Watch",
          "price": 599000,
          "totalSold": 100
        }
      ]
    }
""";

    public static final String POPULAR_PRODUCT_ERROR = """
        {
          "success": false,
          "message": "인기 상품을 조회하는 중 오류가 발생했습니다.",
          "data": null
        }
    """;

    private ProductSwaggerDocs() {}
}
