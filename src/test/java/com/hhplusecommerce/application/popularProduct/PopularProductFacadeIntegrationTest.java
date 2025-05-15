package com.hhplusecommerce.application.popularProduct;

import com.hhplusecommerce.domain.order.OrderItem;
import com.hhplusecommerce.domain.popularProduct.model.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.command.PopularProductCommand;
import com.hhplusecommerce.domain.popularProduct.service.ProductSalesStatsService;
import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductInventory;
import com.hhplusecommerce.domain.product.ProductInventoryRepository;
import com.hhplusecommerce.domain.product.ProductRepository;
import com.hhplusecommerce.support.IntegrationTestSupport;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@SpringBootTest
@Transactional
public class PopularProductFacadeIntegrationTest extends IntegrationTestSupport {

    private static final int STOCK = 10;
    private static final BigDecimal PRICE = BigDecimal.valueOf(10000);
    private static final String CATEGORY = "electronics";

    @Autowired private PopularProductFacade popularProductFacade;
    @Autowired private ProductSalesStatsService productSalesStatsService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductInventoryRepository inventoryRepository;

    @Test
    void 인기상품_조회시_판매수량_기준으로_내림차순_정렬된다() {
        // given
        Product p1 = createAndSaveProduct("립스틱", "메이크업");
        Product p2 = createAndSaveProduct("클렌징폼", "스킨케어");

        recordSales(p1, 10);
        recordSales(p2, 5);

        PopularProductCommand command = new PopularProductCommand(null, null, null, null, LocalDate.now());

        // when
        List<PopularProduct> result = popularProductFacade.getPopularProducts(command);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        assertThat(result.get(0).getProductId()).isEqualTo(p1.getId());
        assertThat(result.get(1).getProductId()).isEqualTo(p2.getId());
    }

    private Product createAndSaveProduct(String name, String category) {
        Product product = Instancio.of(Product.class)
                .set(field("name"), name)
                .set(field("category"), category)
                .set(field("price"), PRICE)
                .create();

        product = productRepository.save(product);

        ProductInventory inventory = ProductInventory.builder()
                .product(product)
                .stock(STOCK)
                .build();

        inventoryRepository.save(inventory);

        return product;
    }

    private void recordSales(Product product, int quantity) {
        OrderItem item = new OrderItem(null, product.getId(), quantity, PRICE, CATEGORY);
        productSalesStatsService.recordSales(List.of(item), LocalDate.now());
    }
}
