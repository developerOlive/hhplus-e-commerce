package com.hhplusecommerce.domain.popularProduct;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인기 상품 도메인
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;
    private String productName;
    private Long price;
    private int totalSold;

    @Builder
    public PopularProduct(Long productId, String productName, Long price, int totalSold) {
        if (productId == null || productId <= 0) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_ID);
        }
        if (productName == null || productName.isBlank()) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_NAME);
        }
        if (price == null || price < 0) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_PRICE);
        }
        if (totalSold < 0) {
            throw new CustomException(ErrorType.INVALID_TOTAL_SOLD_COUNT);
        }

        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.totalSold = totalSold;
    }
}
