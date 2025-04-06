package com.hhplusecommerce.domain.product;

/**
 * 상품 도메인
 */
public record Product(Long id, String name, String category, Long price) {

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
