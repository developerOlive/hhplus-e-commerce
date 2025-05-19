package com.hhplusecommerce.infrastructure.popularProduct.ranking;

import com.hhplusecommerce.domain.popularProduct.command.PopularProductSearchCommand;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.port.PopularProductRankingAggregator;
import com.hhplusecommerce.domain.product.ProductDataResult;
import com.hhplusecommerce.infrastructure.popularProduct.cache.ProductCacheAdapter;
import com.hhplusecommerce.infrastructure.popularProduct.ranking.RedisRankingAggregatorImpl;
import com.hhplusecommerce.infrastructure.popularProduct.ranking.key.RedisRankingKeyFactory;
import com.hhplusecommerce.infrastructure.popularProduct.ranking.zset.RankingZSetAdapter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisRankingAggregatorImplTest {

    private static final String CATEGORY = "test-category";
    private static final String SALE_DATE = "20250515";
    private static final int LIMIT = 3;
    private static final Duration TTL = Duration.ofDays(10);

    @Mock
    RedisRankingKeyFactory keyFactory;

    @Mock
    RankingZSetAdapter zSetAdapter;

    @Mock
    ProductCacheAdapter cacheAdapter;

    @InjectMocks
    RedisRankingAggregatorImpl popularProductRankingAggregator;

    @Nested
    class 상품판매량증가_테스트 {

        @Test
        void 주어진_카테고리와_날짜로_정확히_점수_증가와_TTL_설정이_호출되는지_검증한다() {
            String dailyKey = "ranking:" + CATEGORY + "-" + SALE_DATE;
            when(keyFactory.dailyKey(CATEGORY, SALE_DATE)).thenReturn(dailyKey);

            popularProductRankingAggregator.incrementProductSales(CATEGORY, 123L, SALE_DATE, 5);

            verify(zSetAdapter).incrementScore(dailyKey, 123L, 5);
            verify(zSetAdapter).setExpire(dailyKey, Duration.ofDays(30));
        }
    }

    @Nested
    class 랭킹집계_테스트 {

        @Test
        void 주어진_카테고리와_기간_목록으로_일별키_생성과_합산_및_TTL_설정이_정확히_호출되는지_검증한다() {
            List<Integer> daysList = List.of(3, 7);
            LocalDate now = LocalDate.now();

            for (int days : daysList) {
                List<String> dailyKeys = List.of(
                        "ranking:" + CATEGORY + ":" + now.minusDays(1).toString(),
                        "ranking:" + CATEGORY + ":" + now.minusDays(2).toString(),
                        "ranking:" + CATEGORY + ":" + now.minusDays(3).toString()
                );
                when(keyFactory.dailyKeysFor(now, days, CATEGORY)).thenReturn(dailyKeys);
                when(keyFactory.periodKey(CATEGORY, days)).thenReturn("period-key-" + days);
            }

            popularProductRankingAggregator.aggregateRanking(CATEGORY, daysList);

            for (int days : daysList) {
                List<String> dailyKeys = keyFactory.dailyKeysFor(now, days, CATEGORY);
                String periodKey = keyFactory.periodKey(CATEGORY, days);

                verify(zSetAdapter).unionAndStore(dailyKeys, periodKey);
                verify(zSetAdapter).setExpire(periodKey, TTL);
            }
        }
    }

    @Nested
    class 상위상품ID조회_테스트 {

        @Test
        void 주어진_카테고리와_기간으로_상위_상품ID_목록을_정확히_반환하는지_검증한다() {
            int days = 3;
            String periodKey = "period-key";
            Set<String> productIds = new LinkedHashSet<>(List.of("101", "102", "103"));

            when(keyFactory.periodKey(CATEGORY, days)).thenReturn(periodKey);
            when(zSetAdapter.getTopIds(periodKey, LIMIT)).thenReturn(productIds);

            List<Long> result = popularProductRankingAggregator.getTopProductIds(CATEGORY, days, LIMIT);

            assertThat(result).containsExactly(101L, 102L, 103L);
        }

        @Test
        void 상위_상품ID가_없으면_빈_리스트를_반환하는지_검증한다() {
            int days = 3;
            String periodKey = "period-key";

            when(keyFactory.periodKey(CATEGORY, days)).thenReturn(periodKey);
            when(zSetAdapter.getTopIds(periodKey, LIMIT)).thenReturn(Collections.emptySet());

            List<Long> result = popularProductRankingAggregator.getTopProductIds(CATEGORY, days, LIMIT);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class 상품상세정보_캐시저장_테스트 {

        @Test
        void 상품_상세정보가_cacheAdapter의_save_메서드를_통해_정확히_저장되는지_검증한다() {
            List<ProductDataResult> products = List.of(
                    new ProductDataResult(1L, "상품1", "카테고리", null),
                    new ProductDataResult(2L, "상품2", "카테고리", null)
            );

            popularProductRankingAggregator.saveProductsToCache(products);

            verify(cacheAdapter).save(products);
        }
    }

    @Nested
    class 캐시에서_상품정보조회_테스트 {

        @Test
        void 상품ID_목록으로_캐시에서_정상적으로_상품정보를_반환하는지_검증한다() {
            PopularProductSearchCommand command = new PopularProductSearchCommand(CATEGORY, 3, LIMIT);
            Set<String> productIdStrings = new LinkedHashSet<>(List.of("1", "2", "3"));

            String periodKey = "period-key";
            when(keyFactory.periodKey(CATEGORY, 3)).thenReturn(periodKey);
            when(zSetAdapter.getTopIds(periodKey, LIMIT)).thenReturn(productIdStrings);

            List<PopularProduct> cachedProducts = List.of(
                    PopularProduct.builder().productId(1L).productName("상품1").price(BigDecimal.ZERO).totalSold(10).build(),
                    PopularProduct.builder().productId(2L).productName("상품2").price(BigDecimal.ZERO).totalSold(20).build(),
                    PopularProduct.builder().productId(3L).productName("상품3").price(BigDecimal.ZERO).totalSold(30).build()
            );
            when(cacheAdapter.getFromCache(new ArrayList<>(productIdStrings))).thenReturn(cachedProducts);

            List<PopularProduct> result = popularProductRankingAggregator.getProductsFromCache(command);

            assertThat(result).hasSize(3);
            assertThat(result.get(0).getProductId()).isEqualTo(1L);
        }

        @Test
        void 캐시_조회_결과가_없으면_빈_리스트를_반환하는지_검증한다() {
            PopularProductSearchCommand command = new PopularProductSearchCommand(CATEGORY, 3, LIMIT);
            Set<String> productIdStrings = Collections.emptySet();

            String periodKey = "period-key";
            when(keyFactory.periodKey(CATEGORY, 3)).thenReturn(periodKey);
            when(zSetAdapter.getTopIds(periodKey, LIMIT)).thenReturn(productIdStrings);

            List<PopularProduct> result = popularProductRankingAggregator.getProductsFromCache(command);

            assertThat(result).isEmpty();
        }

        @Test
        void null_명령이_들어오면_빈_리스트를_반환하는지_검증한다() {
            List<PopularProduct> result = popularProductRankingAggregator.getProductsFromCache(null);

            assertThat(result).isEmpty();
        }
    }
}
