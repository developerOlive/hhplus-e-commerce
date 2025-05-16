package com.hhplusecommerce.infrastructure.popularProduct.cache;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.product.ProductDataResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductCacheAdapterTest {

    private static final String 캐시_키_PREFIX = "cache:product:";
    private static final Duration 기본_TTL = Duration.ofDays(10);


    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @Mock
    ObjectMapper objectMapper;

    ProductCacheAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new ProductCacheAdapter(redisTemplate, objectMapper);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    class 저장_메서드_테스트 {

        @Test
        void 정상적인_상품데이터를_JSON으로_직렬화해서_캐시에_저장하는지_검증한다() throws Exception {
            ProductDataResult product = new ProductDataResult(1L, "상품명", "카테고리", null);

            when(objectMapper.writeValueAsString(product)).thenReturn("{json}");

            adapter.save(List.of(product));

            verify(valueOperations).set(eq(캐시_키_PREFIX + "1"), eq("{json}"), eq(기본_TTL));
        }

        @Test
        void 상품데이터_직렬화_중_예외가_발생하면_캐시에_저장하지_않는지_검증한다() throws Exception {
            ProductDataResult product = new ProductDataResult(1L, "상품명", "카테고리", null);

            when(objectMapper.writeValueAsString(product)).thenThrow(new RuntimeException("직렬화 실패"));

            adapter.save(List.of(product));

            verify(valueOperations, never()).set(anyString(), anyString(), any());
        }
    }

    @Nested
    class 조회_메서드_테스트 {

        @Test
        void 캐시에서_정상적인_JSON을_불러와_역직렬화가_성공하는지_검증한다() throws Exception {
            String productId = "1";
            String json = "{\"productId\":1}";

            doReturn(json).when(valueOperations).get(캐시_키_PREFIX + productId);
            doReturn(
                    PopularProduct.builder()
                            .productId(1L)
                            .productName("테스트 상품명")
                            .price(BigDecimal.ZERO)
                            .build()
            ).when(objectMapper).readValue(json, PopularProduct.class);

            List<PopularProduct> 결과 = adapter.getFromCache(List.of(productId));

            assertThat(결과).hasSize(1);
            assertThat(결과.get(0).getProductId()).isEqualTo(1L);
        }

        @Test
        void 캐시에_저장된_값이_없으면_해당_상품은_무시되는지_검증한다() {
            String productId = "1";

            doReturn(null).when(valueOperations).get(캐시_키_PREFIX + productId);

            List<PopularProduct> 결과 = adapter.getFromCache(List.of(productId));

            assertThat(결과).isEmpty();
        }

        @Test
        void 캐시값_역직렬화_중_예외가_발생하면_해당_상품은_무시되는지_검증한다() throws Exception {
            String productId = "1";
            String badJson = "잘못된 json";

            when(valueOperations.get(캐시_키_PREFIX + productId)).thenReturn(badJson);
            when(objectMapper.readValue(badJson, PopularProduct.class)).thenThrow(new RuntimeException("역직렬화 실패"));

            List<PopularProduct> 결과 = adapter.getFromCache(List.of(productId));

            assertThat(결과).isEmpty();
        }
    }
}

