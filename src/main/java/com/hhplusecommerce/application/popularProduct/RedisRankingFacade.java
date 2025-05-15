package com.hhplusecommerce.application.popularProduct;

import com.hhplusecommerce.domain.popularProduct.aggregator.PopularProductRankingAggregator;
import com.hhplusecommerce.domain.popularProduct.command.PopularProductSearchCommand;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.product.ProductDataResult;
import com.hhplusecommerce.infrastructure.popularProduct.cache.ProductCacheAdapter;
import com.hhplusecommerce.infrastructure.popularProduct.ranking.key.RedisRankingKeyFactory;
import com.hhplusecommerce.infrastructure.popularProduct.ranking.zset.RankingZSetAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisRankingFacade implements PopularProductRankingAggregator {

    private final RedisRankingKeyFactory redisRankingKeyFactory;
    private final RankingZSetAdapter rankingZSetAdapter;
    private final ProductCacheAdapter productCacheAdapter;

    private static final Duration TTL = Duration.ofDays(10);

    @Override
    public void aggregateRanking(String category, List<Integer> targetDays) {
        for (int days : targetDays) {
            List<String> dailyKeys = redisRankingKeyFactory.dailyKeysFor(LocalDate.now(), days, category);
            String periodKey = redisRankingKeyFactory.periodKey(category, days);

            rankingZSetAdapter.unionAndStore(dailyKeys, periodKey);
            rankingZSetAdapter.setExpire(periodKey, TTL);
        }
    }

    @Override
    public List<Long> getTopProductIds(String category, int days, int limit) {
        String periodKey = redisRankingKeyFactory.periodKey(category, days);
        Set<String> productIdStrings = rankingZSetAdapter.getTopIds(periodKey, limit);

        if (productIdStrings == null || productIdStrings.isEmpty()) {
            return Collections.emptyList();
        }

        return productIdStrings.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public void saveProductsToCache(List<ProductDataResult> productData) {
        productCacheAdapter.save(productData);
    }

    @Override
    public void incrementProductSales(String category, Long productId, String saleDate, int quantity) {
        String dailyKey = redisRankingKeyFactory.dailyKey(category, saleDate);
        rankingZSetAdapter.incrementScore(dailyKey, productId, quantity);
        rankingZSetAdapter.setExpire(dailyKey, Duration.ofDays(30));
    }

    @Override
    public List<PopularProduct> getProductsFromCache(PopularProductSearchCommand command) {
        if (command == null) return Collections.emptyList();

        String category = command.category();
        int days = command.days();
        int limit = command.limit();

        String periodKey = redisRankingKeyFactory.periodKey(category, days);
        Set<String> productIdStrings = rankingZSetAdapter.getTopIds(periodKey, limit);

        if (productIdStrings == null || productIdStrings.isEmpty()) {
            return Collections.emptyList();
        }

        return productCacheAdapter.load(productIdStrings.stream().toList());
    }
}
