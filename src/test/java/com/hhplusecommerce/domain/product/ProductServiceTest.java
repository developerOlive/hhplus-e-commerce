package com.hhplusecommerce.domain.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductInventoryRepository productInventoryRepository;

    @Test
    void 상품_목록이_정상_조회된다() {
        // given
        ProductsCommand command = new ProductsCommand("testProduct", 1000L, 2000L, "categoryA", 1, 10);
        Pageable pageable = Pageable.unpaged();
        List<Product> mockProducts = List.of(
                new Product(1L, "Product 1", "Category A", BigDecimal.valueOf(1000)),
                new Product(2L, "Product 2", "Category B", BigDecimal.valueOf(2000))
        );
        Page<Product> mockPage = new PageImpl<>(mockProducts, pageable, mockProducts.size());

        when(productRepository.findProducts(command, pageable)).thenReturn(mockPage);
        when(productInventoryRepository.findInventoriesByProductIds(anyList()))
                .thenReturn(List.of(new ProductInventory(1L, 10), new ProductInventory(2L, 20)));

        // when
        Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(mockProducts.size());
        verify(productRepository).findProducts(command, pageable);
        verify(productInventoryRepository).findInventoriesByProductIds(anyList());
    }

    @Test
    void 조회할_상품이_없으면_빈_목록이_반환된다() {
        // given
        ProductsCommand command = new ProductsCommand("nonexistentProduct", 1000L, 2000L, "nonexistentCategory", 1, 10);
        Pageable pageable = Pageable.unpaged();
        Page<Product> emptyProductPage = Page.empty();

        when(productRepository.findProducts(any(ProductsCommand.class), any(Pageable.class)))
                .thenReturn(emptyProductPage);
        when(productInventoryRepository.findInventoriesByProductIds(anyList()))
                .thenReturn(List.of());

        // when
        Page<ProductResult> results = productService.getProductsWithInventory(command, pageable);

        // then
        assertThat(results).isEmpty();
        verify(productRepository).findProducts(command, pageable);
        verify(productInventoryRepository).findInventoriesByProductIds(anyList());
    }
}
