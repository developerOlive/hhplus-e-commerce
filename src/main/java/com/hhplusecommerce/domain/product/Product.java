package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 상품 도메인
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Product(Long id, String name, String category, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_NAME);
        }
        if (category == null || category.isBlank()) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_CATEGORY);
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_PRICE);
        }

        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
