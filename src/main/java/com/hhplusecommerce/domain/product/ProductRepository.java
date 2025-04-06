package com.hhplusecommerce.domain.product;

import com.hhplusecommerce.applicatoin.product.criteria.ProductListCriteria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository {
    List<Product> findByCriteria(ProductListCriteria criteria);
}
