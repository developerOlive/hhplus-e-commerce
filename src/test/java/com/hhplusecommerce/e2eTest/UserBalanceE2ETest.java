package com.hhplusecommerce.e2eTest;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserBalanceE2ETest {

    @LocalServerPort
    int port;

    @Test
    void 잔액충전_후_조회시_반영된_잔액이_조회된다() {
        RestAssured.port = port;

        Long userId = 1L;
        Long chargeAmount = 5_000L;

        // 잔액 충전 요청
        Map<String, Object> chargeRequest = Map.of("amount", chargeAmount);

        Long chargedAmount =
                given().log().all()
                        .contentType(ContentType.JSON)
                        .body(chargeRequest)
                        .when().post("/api/v1/users/{userId}/balance/charge", userId)
                        .then().log().all()
                        .statusCode(200)
                        .extract().<Number>path("data.amount")
                        .longValue();

        Long currentBalance =
                given().log().all()
                        .contentType(ContentType.JSON)
                        .when().get("/api/v1/users/{userId}/balance", userId)
                        .then().log().all()
                        .statusCode(200)
                        .extract().<Number>path("data.amount")
                        .longValue();

        // 검증
        assertThat(currentBalance).isEqualTo(chargedAmount);
    }
}
