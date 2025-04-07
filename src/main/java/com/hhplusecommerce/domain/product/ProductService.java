package com.hhplusecommerce.domain.product;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findProductsByCommand(ProductsCommand command) {
        return productRepository.findProductsByCommand(command);
    }
}
