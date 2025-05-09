package com.hhplusecommerce.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.function.Function;

@RequiredArgsConstructor
public class RedisCacheSupport<T> implements CacheSupport<T> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Function<Object, String> keyBuilder;
    private final TtlStrategy ttlStrategy;

    @Override
    public T get(Object keyParams) {
        String key = keyBuilder.apply(keyParams);
        return (T) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void put(Object keyParams, T data) {
        if (data != null) {
            String key = keyBuilder.apply(keyParams);
            redisTemplate.opsForValue().set(key, data, ttlStrategy.computeTTL(keyParams));
        }
    }

    @Override
    public void evict(Object keyParams) {
        String key = keyBuilder.apply(keyParams);
        redisTemplate.delete(key);
    }
}
