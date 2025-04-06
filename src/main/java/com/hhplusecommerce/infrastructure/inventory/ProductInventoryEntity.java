package com.hhplusecommerce.infrastructure.inventory;

import com.hhplusecommerce.infrastructure.product.ProductEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_inventory")
public class ProductInventoryEntity {

    @Id
    @Column(name = "ref_product_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_product_id")
    private ProductEntity product;

    private Integer inventory;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ProductInventoryEntity(ProductEntity product, Integer inventory) {
        this.product = product;
        this.id = product.getId();
        this.inventory = inventory;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
