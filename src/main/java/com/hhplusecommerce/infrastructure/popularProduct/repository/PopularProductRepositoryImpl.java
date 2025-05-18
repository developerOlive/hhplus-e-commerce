package com.hhplusecommerce.infrastructure.popularProduct.repository;

import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.command.PopularProductCommand;
import com.hhplusecommerce.domain.popularProduct.repository.PopularProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PopularProductRepositoryImpl implements PopularProductRepository {

    private final PopularProductCustomRepository popularProductCustomRepository;

    @Override
    public List<PopularProduct> findTopByCommand(PopularProductCommand command) {
        return popularProductCustomRepository.findTopByCommand(command);
    }
}
