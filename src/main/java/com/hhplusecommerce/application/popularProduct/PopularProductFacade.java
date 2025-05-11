package com.hhplusecommerce.application.popularProduct;

import com.hhplusecommerce.domain.popularProduct.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.PopularProductCommand;
import com.hhplusecommerce.domain.popularProduct.PopularProductRepository;
import com.hhplusecommerce.infrastructure.cache.CacheSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 인기 상품 조회
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PopularProductFacade {

    private final CacheSupport<List<PopularProduct>> popularProductCache;
    private final PopularProductRepository popularProductRepository;

    public List<PopularProduct> getPopularProducts(PopularProductCommand command) {
        // 캐시 먼저 조회
        List<PopularProduct> cached = popularProductCache.get(command);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        // 캐시 MISS → DB 조회
        List<PopularProduct> result = popularProductRepository.findTopByCommand(command);
        if (result == null || result.isEmpty()) {
            return Collections.emptyList();
        }

        // DB 결과 존재 시 캐시 저장
        popularProductCache.put(command, result);

        return result;
    }
}
