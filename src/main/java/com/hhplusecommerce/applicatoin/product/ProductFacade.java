package com.hhplusecommerce.applicatoin.product;

import com.hhplusecommerce.applicatoin.product.criteria.ProductListCriteria;
import com.hhplusecommerce.applicatoin.product.result.ProductResult.ProductListResult;
import com.hhplusecommerce.domain.inventory.ProductInventoryService;
import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final ProductInventoryService productInventoryService;

    public List<ProductListResult> getProducts(ProductListCriteria criteria) {
        List<Product> products = productService.findByCriteria(criteria);

        List<Long> ids = products.stream().map(Product::getId).toList();

        Map<Long, Integer> stockMap = productInventoryService.getStockMap(ids);

        return products.stream()
                .map(product -> new ProductListResult(
                        product.getId(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        stockMap.getOrDefault(product.getId(), 0)
                ))
                .toList();
    }
}
