package com.hhplusecommerce.domain.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * 상품 도메인
 */
@Entity
public class Product {

    @Id
    private Long id;

    private String name;
    private String category;
    private Long price;

    protected Product() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public Long getPrice() {
        return price;
    }
}
