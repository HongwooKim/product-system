package com.coupang.product.infrastructure.persistence.repository;

import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.SKU;
import com.coupang.product.domain.repository.FCProductRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FCProductRepositoryImpl implements FCProductRepository {

    private final FCProductJpaRepository jpaRepository;

    public FCProductRepositoryImpl(FCProductJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FCProduct save(FCProduct fcProduct) {
        return jpaRepository.save(fcProduct);
    }

    @Override
    public Optional<FCProduct> findById(String fcProductId) {
        return jpaRepository.findById(new FCProductId(fcProductId));
    }

    @Override
    public Optional<FCProduct> findBySkuAndWarehouseId(String sku, String warehouseId) {
        return jpaRepository.findBySkuAndWarehouseId(new SKU(sku), new WarehouseId(warehouseId));
    }

    @Override
    public List<FCProduct> findByWarehouseIdAndStatus(WarehouseId warehouseId, FCProductStatus status) {
        return jpaRepository.findByWarehouseIdAndStatus(warehouseId, status);
    }

    @Override
    public List<FCProduct> findBySku(String sku) {
        return jpaRepository.findBySku(new SKU(sku));
    }

    @Override
    public List<FCProduct> findByWarehouseIdAndVelocity(String warehouseId, VelocityClass velocity) {
        return jpaRepository.findByWarehouseIdAndVelocity(new WarehouseId(warehouseId), velocity);
    }

    @Override
    public boolean existsBySkuAndWarehouseId(String sku, String warehouseId) {
        return jpaRepository.existsBySkuAndWarehouseId(new SKU(sku), new WarehouseId(warehouseId));
    }
}
