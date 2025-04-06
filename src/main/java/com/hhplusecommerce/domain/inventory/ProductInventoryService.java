package com.hhplusecommerce.domain.inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductInventoryService {

    private final ProductInventoryRepository productInventoryRepository;

    public Map<Long, Integer> getStockMap(List<Long> productIds) {
        return productInventoryRepository.getInventoryByProductIds(productIds)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getQuantity()
                ));
    }
}
