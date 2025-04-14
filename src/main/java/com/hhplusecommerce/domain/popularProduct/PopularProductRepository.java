package com.hhplusecommerce.domain.popularProduct;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PopularProductRepository {
    List<PopularProduct> findTopByCommand(PopularProductCommand command);
}
