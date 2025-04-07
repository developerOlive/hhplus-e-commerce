package com.hhplusecommerce.applicatoin.product;

import com.hhplusecommerce.domain.inventory.ProductInventoryService;
import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductService;
import com.hhplusecommerce.domain.product.ProductsCommand;
import com.hhplusecommerce.interfaces.product.ProductResponse.ProductsResponse;
import com.hhplusecommerce.interfaces.product.ProductResponse.ProductsResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final ProductInventoryService inventoryService;

    public ProductsResponseWrapper getProducts(ProductsCommand command) {
        List<Product> products = productService.findProductsByCommand(command);

        boolean hasNext = products.size() > command.size();
        List<Product> paginated = hasNext
                ? products.subList(0, command.size())
                : products;

        List<Long> productIds = paginated.stream().map(Product::getId).toList();
        Map<Long, Integer> stockMap = inventoryService.getStockMap(productIds);

        List<ProductsResponse> responses = paginated.stream()
                .map(product -> ProductsResponse.from(
                        ProductsResult.of(product, stockMap.getOrDefault(product.getId(), 0))
                ))
                .toList();

        return new ProductsResponseWrapper(responses, responses.size(), hasNext);
    }
}
