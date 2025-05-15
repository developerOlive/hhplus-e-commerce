package com.hhplusecommerce.infrastructure.product;

import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductRepository;
import com.hhplusecommerce.domain.product.ProductsCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Page<Product> findProducts(ProductsCommand command, Pageable pageable) {
        return productJpaRepository.findProducts(command, pageable);
    }

    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> findProducts(List<Long> productIds) {
        return productJpaRepository.findProducts(productIds);
    }
}
