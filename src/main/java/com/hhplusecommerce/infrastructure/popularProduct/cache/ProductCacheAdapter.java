package com.hhplusecommerce.infrastructure.popularProduct.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.product.ProductDataResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheAdapter {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRODUCT_CACHE_PREFIX = "cache:product:";
    private static final int CACHE_TTL_DAYS = 10;
    private static final Duration TTL = Duration.ofDays(CACHE_TTL_DAYS);

    public void save(List<ProductDataResult> productDataList) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        for (ProductDataResult product : productDataList) {
            String key = PRODUCT_CACHE_PREFIX + product.productId();
            try {
                String json = objectMapper.writeValueAsString(product);
                valueOperations.set(key, json, TTL);
            } catch (Exception e) {
                log.error("상품 캐시 저장 실패 [id={}]: {}", product.productId(), e.getMessage(), e);
            }
        }
    }

    public List<PopularProduct> load(List<String> productIds) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        List<PopularProduct> result = new ArrayList<>();

        for (String productId : productIds) {
            String cacheKey = PRODUCT_CACHE_PREFIX + productId;
            try {
                String json = valueOperations.get(cacheKey);
                if (json == null) continue;
                PopularProduct product = objectMapper.readValue(json, PopularProduct.class);
                result.add(product);
            } catch (Exception e) {
                log.error("상품 캐시 조회 실패 [id={}]: {}", productId, e.getMessage(), e);
            }
        }

        return result;
    }
}
