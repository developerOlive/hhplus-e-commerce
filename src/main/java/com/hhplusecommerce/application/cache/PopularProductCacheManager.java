package com.hhplusecommerce.application.cache;

import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.command.PopularProductCommand;
import com.hhplusecommerce.infrastructure.cache.CacheSupport;
import com.hhplusecommerce.infrastructure.cache.RedisCacheSupport;
import com.hhplusecommerce.infrastructure.cache.EndOfDayTtlStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@Configuration
public class PopularProductCacheManager {

    @Bean
    public CacheSupport<List<PopularProduct>> popularProductCache(RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheSupport<>(
                redisTemplate,
                keyParams -> PopularProductCacheKeyGenerator.build((PopularProductCommand) keyParams, true),
                new EndOfDayTtlStrategy()
        );
    }
}
