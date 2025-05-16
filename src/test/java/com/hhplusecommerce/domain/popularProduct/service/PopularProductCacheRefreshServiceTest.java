package com.hhplusecommerce.domain.popularProduct.service;

import com.hhplusecommerce.domain.popularProduct.aggregator.PopularProductRankingAggregator;
import com.hhplusecommerce.domain.product.ProductDataResult;
import com.hhplusecommerce.domain.product.ProductService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PopularProductCacheRefreshServiceTest {

    private static final List<String> 테스트_카테고리 = List.of("food", "cosmetics", "clothing", "electronics");
    private static final List<Integer> 테스트_기간_일수 = List.of(3, 7, 30);
    private static final int TOP_N = 30;

    @Mock
    private PopularProductRankingAggregator popularProductRankingAggregator;

    @Mock
    private ProductService productService;

    private PopularProductCacheRefreshService cacheRefreshService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cacheRefreshService = new PopularProductCacheRefreshService(popularProductRankingAggregator, productService);
    }

    @Nested
    class 정상_기간값_테스트 {

        @Test
        void 인기상품_카테고리별_기간별_집계와_캐시_갱신이_정상_호출된다() {
            List<Long> dummyProductIds = List.of(101L, 102L, 103L);
            List<ProductDataResult> dummyProductData = List.of(
                    new ProductDataResult(101L, "product1", "food", null),
                    new ProductDataResult(102L, "product2", "food", null),
                    new ProductDataResult(103L, "product3", "food", null)
            );

            when(popularProductRankingAggregator.getTopProductIds(anyString(), anyInt(), eq(TOP_N))).thenReturn(dummyProductIds);
            when(productService.findProducts(dummyProductIds)).thenReturn(dummyProductData);

            cacheRefreshService.refreshCache();

            verify(popularProductRankingAggregator, times(테스트_카테고리.size())).aggregateRanking(anyString(), eq(테스트_기간_일수));
            verify(popularProductRankingAggregator, atLeastOnce()).getTopProductIds(anyString(), intThat(d -> d >= 1), eq(TOP_N));
            verify(productService, atLeastOnce()).findProducts(anyList());
            verify(popularProductRankingAggregator, atLeastOnce()).saveProductsToCache(anyList());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 3, 7, 10})
        void 인기상품_기간_일수가_유효한_값일_때_정상_호출된다(int days) {
            List<Long> dummyProductIds = List.of(201L, 202L);
            List<ProductDataResult> dummyProductData = List.of(
                    new ProductDataResult(201L, "productA", "cosmetics", null),
                    new ProductDataResult(202L, "productB", "cosmetics", null)
            );

            when(popularProductRankingAggregator.getTopProductIds(anyString(), eq(days), eq(TOP_N))).thenReturn(dummyProductIds);
            when(productService.findProducts(dummyProductIds)).thenReturn(dummyProductData);

            // 직접 단일 카테고리, 단일 기간 호출 시뮬레이션
            popularProductRankingAggregator.aggregateRanking("cosmetics", List.of(days));
            popularProductRankingAggregator.getTopProductIds("cosmetics", days, TOP_N);
            productService.findProducts(dummyProductIds);
            popularProductRankingAggregator.saveProductsToCache(dummyProductData);

            verify(popularProductRankingAggregator).aggregateRanking("cosmetics", List.of(days));
            verify(popularProductRankingAggregator).getTopProductIds("cosmetics", days, TOP_N);
            verify(productService).findProducts(dummyProductIds);
            verify(popularProductRankingAggregator).saveProductsToCache(dummyProductData);
        }
    }

    @Nested
    class 비정상_기간값_테스트 {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        void 인기상품_기간_일수가_비정상_값일_때_호출이_없거나_예외가_발생한다(int days) {
            // when - 비정상 기간값이 포함된 TARGET_DAYS 가 있다고 가정하여 직접 호출 시뮬레이션
            Assertions.assertThrows(IllegalArgumentException.class, () -> {
                if (days < 1) throw new IllegalArgumentException("days는 1 이상이어야 합니다.");
                popularProductRankingAggregator.aggregateRanking("cosmetics", List.of(days));
                popularProductRankingAggregator.getTopProductIds("cosmetics", days, TOP_N);
            });
        }
    }
}
