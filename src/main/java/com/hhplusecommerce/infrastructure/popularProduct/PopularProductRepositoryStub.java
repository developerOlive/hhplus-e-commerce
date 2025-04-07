package com.hhplusecommerce.infrastructure.popularProduct;

import com.hhplusecommerce.domain.popularProduct.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.PopularProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PopularProductRepositoryStub implements PopularProductRepository {

    @Override
    public List<PopularProduct> findTopProductsBySales(int limit) {
        return List.of();
    }
}
