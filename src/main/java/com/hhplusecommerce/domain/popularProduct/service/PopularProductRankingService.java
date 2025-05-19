package com.hhplusecommerce.domain.popularProduct.service;

import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.popularProduct.port.PopularProductRankingAggregator;
import com.hhplusecommerce.domain.popularProduct.command.PopularProductSearchCommand;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 인기 상품 집계 및 조회용 도메인 서비스
 */
@Service
@RequiredArgsConstructor
public class PopularProductRankingService {

    private final PopularProductRankingAggregator popularProductRankingAggregator;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 일자별 상품 판매 통계를 누적 기록 (캐싱)
     */
    public void recordSales(List<OrderItem> items) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        for (OrderItem item : items) {
            popularProductRankingAggregator.incrementProductSales(item.getCategory(), item.getProductId(), today, item.getQuantity());
        }
    }

    /**
     * 인기 상품 조회 (캐싱)
     */
    public List<PopularProduct> getTopPopularProducts(PopularProductSearchCommand command) {
        return popularProductRankingAggregator.getProductsFromCache(command);
    }
}
