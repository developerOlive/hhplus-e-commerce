package com.hhplusecommerce.domain.popularProduct.aggregator;

import com.hhplusecommerce.domain.product.ProductDataResult;

import java.util.List;

/**
 * 인기 상품 랭킹 집계를 위한 추상 인터페이스
 */
public interface PopularProductRankingAggregator {

    /**
     * 주어진 카테고리와 기간 리스트 기준으로 기간별 인기상품 랭킹 집계 (날짜별 키 생성, union, expire 포함)
     */
    void aggregateRanking(String category, List<Integer> targetDays);

    /**
     * 주어진 카테고리와 기간(일수) 기준으로 인기상품 랭킹 상위 N개 상품ID 조회
     */
    List<Long> getTopProductIds(String category, int days, int limit);

    /**
     * 인기상품 상세정보를 캐시에 저장
     */
    void saveProductsToCache(List<ProductDataResult> productData);
}
