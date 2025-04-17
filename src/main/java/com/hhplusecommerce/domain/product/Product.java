package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.support.exception.CustomException;
import com.hhplusecommerce.support.exception.ErrorType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ProductInventory inventory;

    @Builder
    public Product(String name, String category, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_NAME);
        }

        if (category == null || category.isBlank()) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_CATEGORY);
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorType.INVALID_PRODUCT_PRICE);
        }

        this.name = name;
        this.category = category;
        this.price = price;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void setInventory(ProductInventory inventory) {
        this.inventory = inventory;
    }
}
