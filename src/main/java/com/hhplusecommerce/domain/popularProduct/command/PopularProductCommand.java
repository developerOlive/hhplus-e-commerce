package com.hhplusecommerce.domain.popularProduct.command;

import java.time.LocalDate;

/**
 * 인기 상품 조회 조건
 */
public record PopularProductCommand(
        Integer limit,         // 조회할 최대 인기 상품 수
        Integer minSold,       // 최소 판매 수량
        Integer days,          // 기준일로부터 몇 일 이내 판매 데이터 조회
        String category,       // 상품 카테고리
        LocalDate baseDate     // days 조건의 기준이 되는 날짜
) {
}
