package com.hhplusecommerce.domain.popularProduct.model;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 상품별 일자별 판매 통계 정보를 관리
 * <p>
 * - 특정 상품의 판매 수량 및 총 판매 금액을 일자 단위로 누적 관리
 * - 결제 완료 시 판매 정보를 기록하며, 추후 인기 상품 조회에 활용됨
 * - 생성 시 기본 수치는 0으로 시작하고, 이후 record 메서드를 통해 누적
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "product_sales_stats")
public class ProductSalesStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private LocalDate saleDate;
    private int quantitySold;
    private BigDecimal totalSalesAmount;

    /**
     * 주어진 상품 ID와 날짜 기준으로 초기 판매 통계 생성
     */
    public static ProductSalesStats initialize(Long productId, LocalDate saleDate) {
        if (productId == null || productId <= 0) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_ID);
        }
        if (saleDate == null) {
            throw new CustomException(ErrorType.INVALID_SALE_DATE);
        }

        return new ProductSalesStats(null, productId, saleDate, 0, BigDecimal.ZERO);
    }

    /**
     * 판매 정보(수량, 금액)를 현재 통계에 반영
     * - 음수도 허용되며, 누적 또는 차감 형태로 적용
     */
    public void record(int quantity, BigDecimal amount) {
        if (amount == null) {
            throw new CustomException(ErrorType.INVALID_SALES_AMOUNT);
        }

        this.quantitySold += quantity;
        this.totalSalesAmount = this.totalSalesAmount.add(amount);
    }
}
