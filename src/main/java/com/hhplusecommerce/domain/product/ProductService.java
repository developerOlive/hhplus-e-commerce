package com.hhplusecommerce.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<Product> getPaginatedProducts(ProductsCommand command, Pageable pageable) {
        return productRepository.findProducts(command, pageable);
    }
}
