package com.hhplusecommerce.infrastructure.popularProduct;

import com.hhplusecommerce.domain.popularProduct.PopularProduct;
import com.hhplusecommerce.domain.popularProduct.PopularProductCommand;
import com.hhplusecommerce.domain.popularProduct.QProductSalesStats;
import com.hhplusecommerce.domain.product.QProduct;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PopularProductCustomRepositoryImpl implements PopularProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PopularProduct> findTopByCommand(PopularProductCommand command) {
        QProduct product = QProduct.product;
        QProductSalesStats stats = QProductSalesStats.productSalesStats;

        BooleanBuilder condition = new BooleanBuilder();

        if (command.category() != null) {
            condition.and(product.category.eq(command.category()));
        }
        if (command.days() != null) {
            LocalDate startDate = LocalDate.now().minusDays(command.days());
            condition.and(stats.saleDate.goe(startDate));
        }
        if (command.minSold() != null) {
            condition.and(stats.quantitySold.goe(command.minSold()));
        }

        return queryFactory
                .select(Projections.constructor(PopularProduct.class,
                        product.id,
                        product.name,
                        product.price,
                        stats.quantitySold.sum().coalesce(0)))
                .from(stats)
                .join(product).on(stats.productId.eq(product.id))
                .where(condition)
                .groupBy(product.id)
                .orderBy(stats.quantitySold.sum().desc())
                .limit(command.limit() != null ? command.limit() : 10)
                .fetch();
    }
}
