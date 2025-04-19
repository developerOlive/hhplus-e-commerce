package com.hhplusecommerce.infrastructure.popularProduct;

import com.hhplusecommerce.domain.popularProduct.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.PopularProductCommand;
import com.hhplusecommerce.domain.popularProduct.PopularProductRepository;
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
