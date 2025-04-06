package com.hhplusecommerce.api.product.docs;

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

    public static final String PRODUCT_DETAIL_SUCCESS = """
        {
          "success": true,
          "message": "요청이 성공적으로 처리되었습니다.",
          "data": {
            "productId": 1,
            "name": "MacBook Pro",
            "category": "노트북",
            "price": 2390000,
            "stock": 10
          }
        }
        """;

    public static final String PRODUCT_NOT_FOUND = """
        {
          "success": false,
          "message": "해당 상품을 찾을 수 없습니다.",
          "data": null
        }
        """;

    public static final String PRODUCT_INVALID_ID = """
        {
          "success": false,
          "message": "유효하지 않은 상품 ID입니다.",
          "data": null
        }
        """;

    private ProductSwaggerDocs() {}
}
