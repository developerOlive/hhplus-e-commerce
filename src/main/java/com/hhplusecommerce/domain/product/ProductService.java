package com.hhplusecommerce.domain.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductResult> getProductsWithInventory(ProductsCommand command, Pageable pageable) {
        Page<Product> pagedProducts = productRepository.findProducts(command, pageable);

        return pagedProducts.map(product -> {
            int stock = product.getInventory() != null ? product.getInventory().getStock() : 0;
            return ProductResult.from(product, stock);
        });
    }
}
