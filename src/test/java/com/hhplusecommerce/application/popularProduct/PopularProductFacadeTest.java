package com.hhplusecommerce.application.popularProduct;

import com.hhplusecommerce.application.cache.PopularProductCacheKeyGenerator;
import com.hhplusecommerce.domain.popularProduct.command.PopularProductCommand;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.repository.PopularProductRepository;
import com.hhplusecommerce.infrastructure.cache.CacheSupport;
import com.hhplusecommerce.infrastructure.cache.EndOfDayTtlStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PopularProductFacadeTest {

    private static final String CATEGORY = "electronics";
    private static final int LIMIT = 10;
    private static final int MIN_SOLD = 100;
    private static final int DAYS = 7;
    private static final LocalDate BASE_DATE = LocalDate.of(2025, 5, 2);

    @Mock
    private CacheSupport<List<PopularProduct>> popularProductCache;

    @Mock
    private PopularProductRepository popularProductRepository;

    @InjectMocks
    private PopularProductFacade popularProductFacade;

    private PopularProductCommand command;

    @BeforeEach
    void setUp() {
        command = new PopularProductCommand(LIMIT, MIN_SOLD, DAYS, CATEGORY, BASE_DATE);
    }

    @Nested
    class 캐시_동작_테스트 {

        private final long PRODUCT_ID = 1L;
        private final String PRODUCT_NAME = "상품1";
        private final BigDecimal PRICE = BigDecimal.valueOf(10000);
        private final int TOTAL_SOLD = 500;

        @Test
        void 캐시에_값이_있으면_DB를_조회하지_않고_바로_반환한다() {
            List<PopularProduct> cached = List.of(
                    PopularProduct.builder()
                            .productId(PRODUCT_ID)
                            .productName(PRODUCT_NAME)
                            .price(PRICE)
                            .totalSold(TOTAL_SOLD)
                            .build()
            );

            when(popularProductCache.get(command)).thenReturn(cached);

            List<PopularProduct> result = popularProductFacade.getPopularProducts(command);

            assertThat(result).isEqualTo(cached);
            verify(popularProductRepository, never()).findTopByCommand(any());
            verify(popularProductCache, never()).put(any(), any());
        }

        @Test
        void 캐시에_없고_DB에서_조회되면_결과를_캐시에_저장한다() {
            List<PopularProduct> dbResult = List.of(
                    PopularProduct.builder()
                            .productId(2L)
                            .productName("상품2")
                            .price(BigDecimal.valueOf(12000))
                            .totalSold(300)
                            .build()
            );

            when(popularProductCache.get(command)).thenReturn(null);
            when(popularProductRepository.findTopByCommand(command)).thenReturn(dbResult);

            List<PopularProduct> result = popularProductFacade.getPopularProducts(command);

            assertThat(result).isEqualTo(dbResult);
            verify(popularProductCache).put(command, dbResult);
        }

        @Test
        void 캐시에_없고_DB에서도_조회되지_않으면_빈_리스트를_반환한다() {
            when(popularProductCache.get(command)).thenReturn(null);
            when(popularProductRepository.findTopByCommand(command)).thenReturn(Collections.emptyList());

            List<PopularProduct> result = popularProductFacade.getPopularProducts(command);

            assertThat(result).isEmpty();
            verify(popularProductCache, never()).put(any(), any());
        }
    }

    @Nested
    class 캐시_키_전략_테스트 {

        @Test
        void 입력값이_정해지면_예상된_캐시_키를_생성한다() {
            String expected = "popular:electronics-100-7-10-2025-05-02";
            String actual = PopularProductCacheKeyGenerator.build(command, true);
            assertThat(actual).isEqualTo(expected);
        }
    }

    @Nested
    class TTL_전략_테스트 {

        private final EndOfDayTtlStrategy strategy = new EndOfDayTtlStrategy();

        @Test
        void 기준일이_오늘이면_자정까지의_TTL이_반환된다() {
            PopularProductCommand todayCommand = new PopularProductCommand(
                    LIMIT, MIN_SOLD, DAYS, CATEGORY, LocalDate.now()
            );

            Duration ttl = strategy.computeTTL(todayCommand);
            assertThat(ttl.getSeconds()).isBetween(1L, 86400L);
        }

        @Test
        void 기준일이_과거면_TTL이_1시간으로_설정된다() {
            PopularProductCommand pastCommand = new PopularProductCommand(
                    LIMIT, MIN_SOLD, DAYS, CATEGORY, LocalDate.now().minusDays(1)
            );

            Duration ttl = strategy.computeTTL(pastCommand);
            assertThat(ttl).isEqualTo(Duration.ofHours(1));
        }
    }
}
