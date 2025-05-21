package com.hhplusecommerce.application.popularProduct;

import com.hhplusecommerce.domain.order.*;
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

    @Autowired private PopularProductFacade popularProductFacade;
    @Autowired private ProductSalesStatsService productSalesStatsService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductInventoryRepository inventoryRepository;
    @Autowired private OrderRepository orderRepository;

    @Test
    void 인기상품_조회시_판매수량_기준으로_내림차순_정렬된다() {
        // given: 두 개의 상품을 생성하고 각각 판매 기록을 저장한다
        Product p1 = createAndSaveProduct("립스틱", "메이크업");
        Product p2 = createAndSaveProduct("클렌징폼", "스킨케어");

        recordSales(p1, 10);  // p1은 10개 판매
        recordSales(p2, 5);   // p2는 5개 판매

        PopularProductCommand command = new PopularProductCommand(null, null, null, null, LocalDate.now());

        // when: 인기 상품을 조회한다
        List<PopularProduct> result = popularProductFacade.getPopularProducts(command);

        // then: 결과가 판매 수량 내림차순으로 정렬되어야 한다
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
        // given: 주문 항목과 주문 생성
        OrderItemCommand itemCommand = new OrderItemCommand(
                product.getId(),
                quantity,
                PRICE,
                product.getCategory()
        );

        OrderCommand orderCommand = new OrderCommand(
                123L,
                null,
                List.of(itemCommand)
        );

        // when: 주문을 생성하고 완료 상태로 변경 후 저장
        Order order = Order.create(orderCommand);
        order.complete();

        Order savedOrder = orderRepository.save(order);

        // then: 상품 판매 통계에 판매 기록 등록
        productSalesStatsService.recordSales(savedOrder.getOrderItems(), LocalDate.now());
    }
}
