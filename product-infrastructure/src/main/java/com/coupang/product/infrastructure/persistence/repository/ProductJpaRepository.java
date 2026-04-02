package com.coupang.product.infrastructure.persistence.repository;

import com.coupang.product.domain.model.product.Product;
import com.coupang.product.domain.model.product.ProductId;
import com.coupang.product.domain.model.product.ProductStatus;
import com.coupang.product.domain.model.product.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, ProductId> {

    @Query("SELECT p FROM Product p WHERE p.sku = :sku")
    Optional<Product> findBySku(@Param("sku") SKU sku);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findByCategoryCode(String categoryCode);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.sku = :sku")
    boolean existsBySku(@Param("sku") SKU sku);
}
