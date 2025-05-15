package com.hhplusecommerce.domain.popularProduct.model;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 인기 상품 도메인
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String productName;
    @Column(precision = 10, scale = 0)
    private BigDecimal price;
    private int totalSold;

    @Builder
    public PopularProduct(Long productId, String productName, BigDecimal price, int totalSold) {
        validate(productId, productName, price, totalSold);
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.totalSold = totalSold;
    }

    public PopularProduct(Long productId, String productName, BigDecimal price, Integer totalSold) {
        this.productId = productId;
        this.productName = productName;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.totalSold = totalSold != null ? totalSold : 0;
    }

    private void validate(Long productId, String productName, BigDecimal price, int totalSold) {
        if (productId == null || productId <= 0) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_ID);
        }
        if (productName == null || productName.isBlank()) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_NAME);
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_PRICE);
        }
        if (totalSold < 0) {
            throw new CustomException(ErrorType.INVALID_TOTAL_SOLD_COUNT);
        }
    }
}
