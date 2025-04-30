package com.hhplusecommerce.integration.product;

import com.hhplusecommerce.support.DbCleaner;
import com.hhplusecommerce.support.IntegrationTestSupport;
import com.hhplusecommerce.domain.product.*;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ProductServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductInventoryRepository productInventoryRepository;

    @Autowired
    private DbCleaner dbCleaner;

    private Product 아이폰;
    private Product 갤럭시;
    private Product 토비의봄;

    @BeforeEach
    void setUp() {
        dbCleaner.execute();
        아이폰 = createProduct("아이폰", "전자기기", new BigDecimal("1000000"), 50);
        갤럭시 = createProduct("갤럭시", "전자기기", new BigDecimal("900000"), 10);
        토비의봄 = createProduct("토비의 봄", "도서", new BigDecimal("30000"), 5);
    }

    private Product createProduct(String name, String category, BigDecimal price, int stock) {
        Product product = Instancio.of(Product.class)
                .set(Select.field(Product::getName), name)
                .set(Select.field(Product::getCategory), category)
                .set(Select.field(Product::getPrice), price)
                .create();

        product = productRepository.save(product);

        ProductInventory inventory = ProductInventory.builder()
                .product(product)
                .stock(stock)
                .build();

        productInventoryRepository.save(inventory);

        return product;
    }

    @Nested
    class 상품_조회_성공 {

        @Test
        void 전체_상품을_조회한다() {
            ProductsCommand command = ProductsCommand.builder().build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            assertAll(
                    () -> assertThat(result).hasSize(3),
                    () -> assertThat(result.getContent()).extracting("name")
                            .containsExactlyInAnyOrder("아이폰", "갤럭시", "토비의 봄")
            );
        }

        @Test
        void 카테고리로_필터링할_수_있다() {
            ProductsCommand command = ProductsCommand.builder()
                    .category("전자기기")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result.getContent()).extracting("name")
                            .containsExactlyInAnyOrder("아이폰", "갤럭시")
            );
        }

        @Test
        void 상품명을_포함하는_키워드로_조회한다() {
            ProductsCommand command = ProductsCommand.builder()
                    .productName("토비")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            assertAll(
                    () -> assertThat(result).hasSize(1),
                    () -> assertThat(result.getContent().get(0).name()).isEqualTo("토비의 봄")
            );
        }

        @Test
        void 가격_범위로_조회할_수_있다() {
            ProductsCommand command = ProductsCommand.builder()
                    .minPrice(new BigDecimal("800000"))
                    .maxPrice(new BigDecimal("950000"))
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            assertAll(
                    () -> assertThat(result).hasSize(1),
                    () -> assertThat(result.getContent().get(0).name()).isEqualTo("갤럭시")
            );
        }

        @Test
        void 여러조건_조합으로_조회할_수_있다() {
            ProductsCommand command = ProductsCommand.builder()
                    .category("전자기기")
                    .productName("갤럭시")
                    .minPrice(new BigDecimal("800000"))
                    .maxPrice(new BigDecimal("950000"))
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            assertAll(
                    () -> assertThat(result).hasSize(1),
                    () -> assertThat(result.getContent().get(0).name()).isEqualTo("갤럭시")
            );
        }
    }

    @Nested
    class 상품_조회_실패 {

        @Test
        void 존재하지_않는_카테고리_조회시_빈_결과가_반환된다() {
            ProductsCommand command = ProductsCommand.builder()
                    .category("없는카테고리")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            assertThat(result).isEmpty();
        }

        @Test
        void 등록된_상품보다_높은_최소가격을_설정하면_빈결과가_반환된다() {
            // given
            BigDecimal maxRegisteredPrice = new BigDecimal("1000000");
            BigDecimal higherThanMax = maxRegisteredPrice.add(BigDecimal.valueOf(1));

            ProductsCommand command = ProductsCommand.builder()
                    .minPrice(higherThanMax)
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            // then
            assertThat(result).isEmpty();
        }


        @Test
        void 상품명에_일치하는_키워드가_없으면_빈_결과가_반환된다() {
            ProductsCommand command = ProductsCommand.builder()
                    .productName("없는이름")
                    .build();
            Pageable pageable = PageRequest.of(0, 10);

            Page<ProductResult> result = productService.getProductsWithInventory(command, pageable);

            assertThat(result).isEmpty();
        }
    }
}
