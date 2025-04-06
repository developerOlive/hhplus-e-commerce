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
public class PopularProductE2ETest {

    @LocalServerPort
    int port;

    @Test
    void 인기상품_조회시_5개이상_응답된다() {
        RestAssured.port = port;

        // 인기 상품 조회 요청
        List<Map<String, Object>> response =
                given()
                        .log().all()
                        .contentType(ContentType.JSON)
                        .when().get("/api/v1/products/popular")
                        .then().log().all()
                        .statusCode(200)
                        .extract().path("data");

        // 검증: 응답 개수 5개 이상, 상품 ID 존재 확인
        assertThat(response).hasSizeGreaterThanOrEqualTo(5);
        assertThat(response.get(0).get("productId")).isNotNull();
        assertThat(response.get(0).get("totalSold")).isNotNull();
    }
}
