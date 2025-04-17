package com.hhplusecommerce.infrastructure.product;

import com.hhplusecommerce.domain.product.Product;
import com.hhplusecommerce.domain.product.ProductsCommand;
import com.hhplusecommerce.domain.product.ProductSortOption;
import com.hhplusecommerce.domain.product.QProduct;
import com.hhplusecommerce.domain.product.QProductInventory;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Product> findProducts(ProductsCommand command, Pageable pageable) {
        QProduct product = QProduct.product;
        QProductInventory inventory = QProductInventory.productInventory;

        List<Product> content = queryFactory
                .selectFrom(product)
                .leftJoin(product.inventory, inventory).fetchJoin()
                .where(buildConditions(command, product))
                .orderBy(getOrderSpecifier(command, product))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(buildConditions(command, product))
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanBuilder buildConditions(ProductsCommand command, QProduct product) {
        BooleanBuilder builder = new BooleanBuilder();
        if (command.getCategory() != null) {
            builder.and(product.category.eq(command.getCategory()));
        }

        return builder;
    }

    private OrderSpecifier<?> getOrderSpecifier(ProductsCommand command, QProduct product) {
        ProductSortOption sortOption = command.getSortOption();

        return Objects.requireNonNullElse(sortOption, ProductSortOption.LATEST).getOrderSpecifier();
    }
}
