package com.hhplusecommerce.domain.product;

import java.util.List;

public interface ProductRepository {
    List<Product> findProductsByCommand(ProductsCommand command);
}
