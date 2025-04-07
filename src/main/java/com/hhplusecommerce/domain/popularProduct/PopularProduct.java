package com.hhplusecommerce.domain.popularProduct;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 인기 상품 도메인
 */
@Entity
public class PopularProduct {

    @Id
    private Long productId;

    private String productName;
    private Long price;
    private int totalSold;

    protected PopularProduct() {
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Long getPrice() {
        return price;
    }

    public int getTotalSold() {
        return totalSold;
    }
}
