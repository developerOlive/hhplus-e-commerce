package com.hhplusecommerce.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Page<Product> findProductsWithInventory(ProductsCommand command, Pageable pageable);
}
