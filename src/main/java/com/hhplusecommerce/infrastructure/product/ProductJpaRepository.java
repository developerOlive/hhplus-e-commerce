package com.hhplusecommerce.infrastructure.product;

import com.hhplusecommerce.domain.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, Long>, ProductCustomRepository {

}
