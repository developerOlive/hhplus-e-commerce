package com.hhplusecommerce.interfaces.product;

import com.hhplusecommerce.domain.popularProduct.service.PopularProductCacheRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PopularProductCacheRefreshScheduler {

    private final PopularProductCacheRefreshService popularProductCacheRefreshService;

    /**
     * N일간 인기상품 조회 성능 향상을 위해 캐시를 갱신
     */
    @Scheduled(cron = "0 15 0 * * *")
    public void refreshPopularProductCache() {
        popularProductCacheRefreshService.refreshCache();
    }
}
