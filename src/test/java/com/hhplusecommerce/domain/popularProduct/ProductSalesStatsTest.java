package com.hhplusecommerce.domain.popularProduct;

import com.hhplusecommerce.domain.popularProduct.model.ProductSalesStats;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProductSalesStatsTest {

    private static final long PRODUCT_ID = 1L;
    private static final LocalDate TODAY = LocalDate.now();
    private static final int FIRST_QUANTITY = 3;
    private static final int SECOND_QUANTITY = 2;
    private static final int EXPECTED_TOTAL_QUANTITY = 5;
    private static final BigDecimal FIRST_AMOUNT = new BigDecimal("1000");
    private static final BigDecimal SECOND_AMOUNT = new BigDecimal("500");
    private static final BigDecimal EXPECTED_TOTAL_AMOUNT = new BigDecimal("1500");


    static ProductSalesStats 기본통계() {
        return ProductSalesStats.initialize(PRODUCT_ID, TODAY);
    }

    @Nested
    class 판매기록_누적 {

        @ParameterizedTest
        @CsvSource({
                "1, 1000",
                "5, 15000",
                "10, 0"
        })
        void 수량과_금액을_정상적으로_누적한다(int quantity, BigDecimal amount) {
            ProductSalesStats stats = 기본통계();

            stats.record(quantity, amount);

            assertThat(stats.getQuantitySold()).isEqualTo(quantity);
            assertThat(stats.getTotalSalesAmount()).isEqualByComparingTo(amount);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 0",
                "-1, 100",
                "3, -500"
        })
        void 수량이나_금액이_음수인_경우에도_누적이_가능하다(int quantity, BigDecimal amount) {
            ProductSalesStats stats = 기본통계();

            stats.record(quantity, amount);

            assertThat(stats.getQuantitySold()).isEqualTo(quantity);
            assertThat(stats.getTotalSalesAmount()).isEqualByComparingTo(amount);
        }

        @Test
        void 여러번_누적하면_값이_합산된다() {
            ProductSalesStats stats = 기본통계();

            stats.record(FIRST_QUANTITY, FIRST_AMOUNT);
            stats.record(SECOND_QUANTITY, SECOND_AMOUNT);

            assertThat(stats.getQuantitySold()).isEqualTo(EXPECTED_TOTAL_QUANTITY);
            assertThat(stats.getTotalSalesAmount()).isEqualByComparingTo(EXPECTED_TOTAL_AMOUNT);
        }
    }
}
