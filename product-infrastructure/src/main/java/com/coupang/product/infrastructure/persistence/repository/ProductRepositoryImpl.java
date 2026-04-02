package com.coupang.product.infrastructure.persistence.repository;

import com.coupang.product.domain.model.product.Product;
import com.coupang.product.domain.model.product.ProductId;
import com.coupang.product.domain.model.product.ProductStatus;
import com.coupang.product.domain.model.product.SKU;
import com.coupang.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository jpaRepository;

    public ProductRepositoryImpl(ProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(String productId) {
        return jpaRepository.findById(new ProductId(productId));
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return jpaRepository.findBySku(new SKU(sku));
    }

    @Override
    public List<Product> findByStatus(ProductStatus status) {
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Product> findByCategoryCode(String categoryCode) {
        return jpaRepository.findByCategoryCode(categoryCode);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(new SKU(sku));
    }
}
