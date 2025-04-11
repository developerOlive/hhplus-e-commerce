package com.hhplusecommerce.domain.popularProduct;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 인기 상품 조회 도메인 서비스
 */
@Service
@RequiredArgsConstructor
public class PopularProductService {

    private final PopularProductRepository popularProductRepository;

    public List<PopularProduct> getPopularProducts(PopularProductCommand command) {
        return popularProductRepository.findTopByCommand(command);
    }
}
