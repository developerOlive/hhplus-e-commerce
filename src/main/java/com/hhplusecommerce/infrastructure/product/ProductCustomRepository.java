package com.hhplusecommerce.infrastructure.product;

import com.hhplusecommerce.domain.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.hhplusecommerce.domain.product.ProductsCommand;

public interface ProductCustomRepository {
    Page<Product> findProducts(ProductsCommand command, Pageable pageable);
}
