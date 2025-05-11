package com.hhplusecommerce.support;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

@Component
@Profile("test")
@RequiredArgsConstructor
public class RedisCacheCleaner {

    private final StringRedisTemplate stringRedisTemplate;

    private static final List<String> CACHE_PREFIXES = List.of(
            "popular:"
    );

    public void clean() {
        for (String prefix : CACHE_PREFIXES) {
            deleteByPrefix(prefix);
        }
    }

    private void deleteByPrefix(String prefix) {
        Set<String> keys = stringRedisTemplate.keys(prefix + "*");
        if (!CollectionUtils.isEmpty(keys)) {
            stringRedisTemplate.delete(keys);
        }
    }
}
