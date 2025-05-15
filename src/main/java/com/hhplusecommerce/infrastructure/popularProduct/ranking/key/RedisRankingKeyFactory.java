package com.hhplusecommerce.infrastructure.popularProduct.ranking.key;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Redis 키 생성 책임을 갖는 Key Factory
 */
@Component
public class RedisRankingKeyFactory {

    private static final String PREFIX = "ranking";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 주어진 카테고리와 기간(days)에 대응하는
     * 기간별 인기상품 랭킹 Redis 키를 생성
     *
     * (ex) categoryName = "electronics", days = 3
     * 결과 : "ranking:electronics:day3"
     */
    public String periodKey(String categoryName, int days) {
        String safeCategory = (categoryName == null || categoryName.isBlank()) ? "all" : categoryName;
        return String.format("%s:%s:day%d", PREFIX, safeCategory, days);
    }

    /**
     * 기준 날짜부터 과거 days 일치하는 일별 인기상품 Redis 키 리스트 생성
     *
     * (ex) referenceDate=2025-05-15, days=3, category="electronics"
     * 반환 값:
     * ["ranking:electronics:2025-05-14",
     *  "ranking:electronics:2025-05-13",
     *  "ranking:electronics:2025-05-12"]
     */
    public List<String> dailyKeysFor(LocalDate baseDate, int days, String categoryName) {
        String category = (categoryName == null || categoryName.isBlank()) ? "all" : categoryName;
        return IntStream.rangeClosed(1, days)
                .mapToObj(i -> baseDate.minusDays(i).format(FORMATTER))
                .map(date -> String.format("%s:%s:%s", PREFIX, category, date))
                .toList();
    }

    /**
     * 특정 카테고리와 일자에 해당하는 일별 랭킹 키 생성
     *
     * (ex) category = "electronics", saleDate = "2025-05-15"
     * 결과 : "ranking:electronics-20250515"
     */
    public String dailyKey(String categoryName, String saleDate) {
        String cat = (categoryName == null || categoryName.isBlank()) ? "all" : categoryName;
        return String.format("%s:%s-%s", PREFIX, cat, saleDate);
    }
}
