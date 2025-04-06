package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.applicatoin.product.criteria.ProductListCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findByCriteria(ProductListCriteria criteria) {
        return productRepository.findByCriteria(criteria);
    }
}
