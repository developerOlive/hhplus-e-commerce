package com.hhplusecommerce.e2eTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductE2ETest {

    @LocalServerPort
    int port;

    @Test
    void 상품목록_조회_및_상세조회_응답이_정상이다() {
        RestAssured.port = port;

        // 상품 목록 조회
        List<Map<String, Object>> products = given()
                .log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/products")
                .then().log().all()
                .statusCode(200)
                .extract().path("data");

        assertThat(products).isNotEmpty();

        // 첫 번째 상품 ID로 상세 조회
        Long productId = ((Number) products.get(0).get("productId")).longValue();

        Map<String, Object> productDetail = given()
                .log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/products/{productId}", productId)
                .then().log().all()
                .statusCode(200)
                .extract().path("data");

        assertThat(((Number) productDetail.get("productId")).longValue()).isEqualTo(productId);
        assertThat(productDetail.get("name")).isNotNull();
        assertThat(productDetail.get("price")).isNotNull();
    }
}
