package com.hhplusecommerce.domain.popularProduct;

import jakarta.persistence.Entity;
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
    private Long productId;

    private String productName;

    private Long price;

    private int totalSold;

    @Builder
    public PopularProduct(Long productId, String productName, Long price, int totalSold) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.totalSold = totalSold;
    }
}
