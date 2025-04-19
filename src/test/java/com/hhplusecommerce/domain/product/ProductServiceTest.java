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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Test
    void 상품_목록이_정상_조회된다() {
        // given
        ProductsCommand command = ProductsCommand.builder()
                .productName("testProduct")
                .minPrice(BigDecimal.valueOf(1000))
                .maxPrice(BigDecimal.valueOf(2000))
                .category("categoryA")
                .page(1)
                .size(10)
                .build();

        Pageable pageable = Pageable.unpaged();

        List<Product> mockProducts = List.of(
                Product.builder().name("Product 1").category("Category A").price(BigDecimal.valueOf(1000)).build(),
                Product.builder().name("Product 2").category("Category B").price(BigDecimal.valueOf(2000)).build()
        );

        Page<Product> mockPage = new PageImpl<>(mockProducts, pageable, mockProducts.size());

        // when
        when(productRepository.findProducts(command, pageable)).thenReturn(mockPage);
        Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(mockProducts.size());
        verify(productRepository).findProducts(command, pageable);
    }

    @Test
    void 조회할_상품이_없으면_빈_목록이_반환된다() {
        // given
        ProductsCommand command = ProductsCommand.builder()
                .productName("nonexistentProduct")
                .minPrice(BigDecimal.valueOf(1000))
                .maxPrice(BigDecimal.valueOf(2000))
                .category("nonexistentCategory")
                .page(1)
                .size(10)
                .build();

        Pageable pageable = Pageable.unpaged();
        Page<Product> emptyProductPage = Page.empty();

        when(productRepository.findProducts(any(ProductsCommand.class), any(Pageable.class)))
                .thenReturn(emptyProductPage);

        // when
        Page<ProductResult> results = productService.getProductsWithInventory(command, pageable);

        // then
        assertThat(results).isEmpty();
        verify(productRepository).findProducts(command, pageable);
    }
}
