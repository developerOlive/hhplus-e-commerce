package com.hhplusecommerce.domain.popularProduct.service;

import com.hhplusecommerce.domain.popularProduct.aggregator.PopularProductRankingAggregator;
import com.hhplusecommerce.domain.product.ProductDataResult;
import com.hhplusecommerce.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PopularProductCacheRefreshService {

    private final PopularProductRankingAggregator popularProductRankingAggregator;
    private final ProductService productService;

    // 추후 확장성 고려 시, 아래 리스트 데이터는 DB 또는 Redis 등에서 동적으로 로딩
    private static final List<String> TARGET_CATEGORIES = List.of("food", "cosmetics", "clothing", "electronics");
    private static final List<Integer> TARGET_DAYS = List.of(3, 7, 30);

    public void refreshCache() {
        for (String category : TARGET_CATEGORIES) {
            popularProductRankingAggregator.aggregateRanking(category, TARGET_DAYS);

            for (int days : TARGET_DAYS) {
                List<Long> productIds = popularProductRankingAggregator.getTopProductIds(category, days, 30);
                List<ProductDataResult> productData = productService.findProducts(productIds);
                popularProductRankingAggregator.saveProductsToCache(productData);
            }
        }
    }
}
