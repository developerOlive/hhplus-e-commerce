package com.hhplusecommerce.infrastructure.popularProduct;

import com.hhplusecommerce.domain.popularProduct.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.PopularProductCommand;

import java.util.List;

public interface PopularProductCustomRepository {
    List<PopularProduct> findTopByCommand(PopularProductCommand command);
}
