package com.hhplusecommerce.applicatoin.product;

import com.hhplusecommerce.domain.inventory.ProductInventoryService;
import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductService;
import com.hhplusecommerce.domain.product.ProductsCommand;
import com.hhplusecommerce.interfaces.product.ProductResponse.ProductsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final ProductInventoryService inventoryService;

    public Page<ProductsResponse> getProducts(ProductsCommand command, Pageable pageable) {
        Page<Product> productPage = productService.getPaginatedProducts(command, pageable);

        List<ProductsResponse> responseList = productPage.getContent().stream()
                .map(product -> {
                    int stock = inventoryService.getStockMap(List.of(product.getId()))
                            .getOrDefault(product.getId(), 0);
                    return ProductsResponse.from(product, stock);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responseList, pageable, productPage.getTotalElements());
    }
}
