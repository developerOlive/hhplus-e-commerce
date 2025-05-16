package com.hhplusecommerce.domain.popularProduct.repository;

import com.hhplusecommerce.domain.popularProduct.command.PopularProductCommand;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;

import java.util.List;

/**
 * 기존 PopularProductRepository에 상품 ID 리스트 조회 추가
 */
public interface PopularProductRepository {

    List<PopularProduct> findTopByCommand(PopularProductCommand command);
}
