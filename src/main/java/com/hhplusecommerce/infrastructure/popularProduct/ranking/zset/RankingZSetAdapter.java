package com.hhplusecommerce.infrastructure.popularProduct.ranking.zset;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingZSetAdapter {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 점수 증가
     */
    public void incrementScore(String key, Long productId, int quantity) {
        redisTemplate.opsForZSet().incrementScore(key, String.valueOf(productId), quantity);
    }

    /**
     * 합산 저장
     */
    public void unionAndStore(List<String> sourceKeys, String destKey) {
        if (sourceKeys.isEmpty()) return;

        if (sourceKeys.size() == 1) {
            redisTemplate.rename(sourceKeys.get(0), destKey);
            return;
        }

        ZSetOperations<String, String> ops = redisTemplate.opsForZSet();
        ops.unionAndStore(sourceKeys.get(0), sourceKeys.get(1), destKey);
        for (int i = 2; i < sourceKeys.size(); i++) {
            ops.unionAndStore(destKey, sourceKeys.get(i), destKey);
        }
    }

    /**
     * 상위 조회
     */
    public Set<String> getTopIds(String key, int limit) {
        return redisTemplate.opsForZSet().reverseRange(key, 0, limit - 1);
    }

    /**
     * TTL 설정
     */
    public void setExpire(String key, Duration ttl) {
        redisTemplate.expire(key, ttl);
    }
}
