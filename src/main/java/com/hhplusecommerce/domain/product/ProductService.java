package com.hhplusecommerce.domain.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductInventoryRepository inventoryRepository;

    public Page<ProductResult> getProductsWithInventory(ProductsCommand command, Pageable pageable) {
        Page<Product> pagedProducts = productRepository.findProducts(command, pageable);

        Map<Long, ProductInventory> inventoryMap = inventoryRepository.findAllByProductIdIn(
                pagedProducts.getContent().stream()
                        .map(Product::getId)
                        .toList()
        ).stream().collect(Collectors.toMap(
                inv -> inv.getProduct().getId(),
                Function.identity()
        ));

        return pagedProducts.map(product -> {
            ProductInventory inventory = inventoryMap.get(product.getId());
            int stock = inventory != null ? inventory.getStock() : 0;
            return ProductResult.from(product, stock);
        });
    }
}
