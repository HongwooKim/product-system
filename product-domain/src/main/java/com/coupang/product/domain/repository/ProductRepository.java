package com.coupang.product.domain.repository;

import com.coupang.product.domain.model.product.Product;
import com.coupang.product.domain.model.product.ProductStatus;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String productId);

    Optional<Product> findBySku(String sku);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategoryCode(String categoryCode);

    boolean existsBySku(String sku);
}
