package com.hhplusecommerce.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Page<Product> findProducts(ProductsCommand command, Pageable pageable);
}
