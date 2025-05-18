package com.hhplusecommerce.application.cache;

import com.hhplusecommerce.domain.popularProduct.command.PopularProductCommand;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 인기 상품 캐시 키 생성기
 * Format: popular:{카테고리}-{minSold}-{days}-{limit}-{baseDate}
 * (ex) popular:electronics-100-7-10-2025-05-02
 */
public class PopularProductCacheKeyGenerator {

    private static final String POPULAR_CACHE_KEY_PREFIX = "popular:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static String build(PopularProductCommand command, boolean withPrefix) {
        Map<String, String> parts = new LinkedHashMap<>();
        parts.put("category", command.category() != null ? command.category() : "all");
        parts.put("minSold", String.valueOf(command.minSold() != null ? command.minSold() : 0));
        parts.put("days", String.valueOf(command.days() != null ? command.days() : 30));
        parts.put("limit", String.valueOf(command.limit() != null ? command.limit() : 10));
        parts.put("baseDate", command.baseDate() != null ? command.baseDate().format(DATE_FORMATTER) : "none");

        String keyBody = String.join("-", parts.values());

        return withPrefix ? POPULAR_CACHE_KEY_PREFIX + keyBody : keyBody;
    }
}
