package com.hhplusecommerce.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductInventoryRepository productInventoryRepository;

    public Page<ProductResult> getProductsWithInventory(ProductsCommand command, Pageable pageable) {
        Page<Product> pagedProducts = productRepository.findProducts(command, pageable);

        List<ProductInventory> productInventories = productInventoryRepository.findInventoriesByProductIds(
                pagedProducts.stream().map(Product::getId).toList()
        );

        return pagedProducts.map(product -> {
            int stock = productInventories.stream()
                    .filter(inventory -> inventory.getProductId().equals(product.getId()))
                    .findFirst()
                    .map(ProductInventory::getStock)
                    .orElse(0);
            return ProductResult.from(product, stock);
        });
    }
}
