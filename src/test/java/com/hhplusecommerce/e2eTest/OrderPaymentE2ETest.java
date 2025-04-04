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
public class OrderPaymentE2ETest {

    @LocalServerPort
    int port;

    @Test
    void 주문하고_결제까지_응답_흐름이_정상이다() {
        RestAssured.port = port;

        // 주문 요청
        Map<String, Object> orderRequest = Map.of(
                "userId", 1L,
                "orderItems", List.of(
                        Map.of("productId", 10L, "quantity", 2)
                ),
                "couponIssueId", 5001L
        );

        Long orderId = given()
                .contentType(ContentType.JSON)
                .body(orderRequest)
                .when().post("/api/v1/orders")
                .then().statusCode(200)
                .extract().<Number>path("data.orderId")
                .longValue();

        // 결제 요청
        Map<String, Object> paymentRequest = Map.of(
                "orderId", orderId,
                "paymentGateway", "CREDIT_CARD"
        );

        String status = given()
                .contentType(ContentType.JSON)
                .body(paymentRequest)
                .when().post("/api/v1/payments")
                .then().statusCode(200)
                .extract().path("data.status");

        // 검증
        assertThat(status).isEqualTo("SUCCESS");
    }
}
