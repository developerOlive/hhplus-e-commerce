package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.interfaces.product.ProductResponse.ProductsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository {
    Page<ProductsResponse> findProductsWithInventory(ProductsCommand command, Pageable pageable);
}
