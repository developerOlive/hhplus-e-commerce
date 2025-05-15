package com.hhplusecommerce.domain.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Page<Product> findProducts(ProductsCommand command, Pageable pageable);

    Optional<Product> findById(Long productId);

    Product save(Product product);

    List<Product> findProducts(List<Long> productIds);
}
