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
public class CouponE2ETest {

    @LocalServerPort
    int port;

    @Test
    void 쿠폰_발급후_목록조회시_포함되어있다() {
        RestAssured.port = port;

        Long userId = 1L;
        Long couponId = 1L; // 고정된 mock 응답과 일치

        // 1. 쿠폰 발급 요청
        Long couponIssueId =
                given().log().all()
                        .contentType(ContentType.JSON)
                        .when().post("/api/v1/users/{userId}/coupons/{couponId}/issue", userId, couponId)
                        .then().log().all()
                        .statusCode(200)
                        .extract().<Number>path("data.couponIssueId")
                        .longValue();

        assertThat(couponIssueId).isGreaterThan(0);

        // 2. 보유 쿠폰 목록 조회
        List<Map<String, Object>> coupons =
                given().log().all()
                        .contentType(ContentType.JSON)
                        .when().get("/api/v1/users/{userId}/coupons", userId)
                        .then().log().all()
                        .statusCode(200)
                        .extract().path("data");

        // 3. 검증: 발급된 쿠폰이 목록에 포함되는지 확인
        boolean containsIssued = coupons.stream()
                .anyMatch(coupon -> ((Number) coupon.get("couponId")).longValue() == couponId);

        assertThat(containsIssued).isTrue();
    }
}
