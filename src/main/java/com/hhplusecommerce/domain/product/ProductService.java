package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.interfaces.product.ProductResponse.ProductsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductsResponse> getProducts(ProductsCommand command, Pageable pageable) {
        return productRepository.findProductsWithInventory(command, pageable);
    }
}
