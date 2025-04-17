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
                .category("ELECTRONICS")
                .sortOption(ProductSortOption.PRICE_ASC)
                .page(0)
                .size(10)
                .build();

        Pageable pageable = Pageable.unpaged();

        List<Product> mockProducts = List.of(
                Product.builder().name("Product 1").category("ELECTRONICS").price(BigDecimal.valueOf(1000)).build(),
                Product.builder().name("Product 2").category("ELECTRONICS").price(BigDecimal.valueOf(2000)).build()
        );

        Page<Product> mockPage = new PageImpl<>(mockProducts, pageable, mockProducts.size());

        // when
        when(productRepository.findProducts(command, pageable)).thenReturn(mockPage);
        Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(productRepository).findProducts(command, pageable);
    }

    @Test
    void 조회할_상품이_없으면_빈_목록이_반환된다() {
        // given
        ProductsCommand command = ProductsCommand.builder()
                .category("EMPTY_CATEGORY")
                .sortOption(ProductSortOption.LATEST)
                .page(0)
                .size(10)
                .build();

        Pageable pageable = Pageable.unpaged();
        Page<Product> emptyPage = Page.empty();

        when(productRepository.findProducts(any(ProductsCommand.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        // when
        Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

        // then
        assertThat(result).isEmpty();
        verify(productRepository).findProducts(command, pageable);
    }
}
