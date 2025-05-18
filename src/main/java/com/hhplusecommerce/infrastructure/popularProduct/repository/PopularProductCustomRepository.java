package com.hhplusecommerce.infrastructure.popularProduct.repository;

import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.command.PopularProductCommand;

import java.util.List;

public interface PopularProductCustomRepository {
    List<PopularProduct> findTopByCommand(PopularProductCommand command);
}
