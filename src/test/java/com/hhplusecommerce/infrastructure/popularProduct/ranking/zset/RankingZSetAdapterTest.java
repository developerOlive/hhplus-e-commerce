package com.hhplusecommerce.infrastructure.popularProduct.ranking.zset;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingZSetAdapterTest {

    private static final String 테스트_단일키 = "key-single";
    private static final String 결과_키 = "dest-key";
    private static final String 상위키 = "top-key";
    private static final Duration 하루_기간 = Duration.ofDays(1);

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    RankingZSetAdapter adapter;

    @Nested
    class 점수증가_테스트 {

        @Test
        void 주어진_키와_상품ID로_ZSet_점수가_증가하는지_검증한다() {
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

            adapter.incrementScore(테스트_단일키, 1L, 10);

            verify(zSetOperations).incrementScore(테스트_단일키, "1", 10);
        }
    }

    @Nested
    class 합산저장_테스트 {

        @Test
        void 키가_하나일_때는_redis_rename이_한번만_호출되는지_검증한다() {
            // rename 호출 시 opsForZSet() 호출하지 않으니 stubbing 제거
            adapter.unionAndStore(List.of(테스트_단일키), 결과_키);

            verify(redisTemplate).rename(테스트_단일키, 결과_키);
        }

        @Test
        void 키가_여러개일_때는_합산연산이_순서대로_정확히_호출되는지_검증한다() {
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

            List<String> keys = List.of("k1", "k2", "k3");
            adapter.unionAndStore(keys, 결과_키);

            InOrder inOrder = inOrder(zSetOperations);
            inOrder.verify(zSetOperations).unionAndStore("k1", "k2", 결과_키);
            inOrder.verify(zSetOperations).unionAndStore(결과_키, "k3", 결과_키);
        }

        @Test
        void 빈_키_목록이_들어오면_아무_작업도_하지_않는지_검증한다() {
            adapter.unionAndStore(List.of(), 결과_키);

            verifyNoInteractions(redisTemplate, zSetOperations);
        }
    }

    @Nested
    class 만료시간설정_테스트 {

        @Test
        void 유효한_키에_올바른_TTL로_만료시간이_설정되는지_검증한다() {
            adapter.setExpire(상위키, 하루_기간);

            verify(redisTemplate).expire(상위키, 하루_기간);
        }
    }

    @Nested
    class 상위아이디조회_테스트 {

        @Test
        void 상위N개의_아이디가_정확히_조회되는지_검증한다() {
            Set<String> 예상값 = Set.of("1", "2", "3");

            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.reverseRange(상위키, 0, 2)).thenReturn(예상값);

            Set<String> 결과 = adapter.getTopIds(상위키, 3);

            assertThat(결과).hasSize(3);
            assertThat(결과).containsExactlyInAnyOrderElementsOf(예상값);
        }

        @Test
        void 요청한_상위개수가_0일_때는_빈_집합을_반환하는지_검증한다() {
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.reverseRange(상위키, 0, -1)).thenReturn(Set.of());

            Set<String> 결과 = adapter.getTopIds(상위키, 0);

            assertThat(결과).isEmpty();
        }

        @Test
        void 요청한_상위개수가_음수일_때는_빈_집합을_반환하는지_검증한다() {
            when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
            when(zSetOperations.reverseRange(eq(상위키), anyLong(), anyLong())).thenReturn(Set.of());

            Set<String> 결과 = adapter.getTopIds(상위키, -5);

            assertThat(결과).isEmpty();
        }
    }
}
