package com.coupang.product.domain.repository;

import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.model.fcproduct.FCProductStatus;
import com.coupang.product.domain.model.fcproduct.VelocityClass;

import java.util.List;
import java.util.Optional;

public interface FCProductRepository {

    FCProduct save(FCProduct fcProduct);

    Optional<FCProduct> findById(String fcProductId);

    Optional<FCProduct> findBySkuAndWarehouseId(String sku, String warehouseId);

    List<FCProduct> findByWarehouseIdAndStatus(
            com.coupang.product.domain.model.fcproduct.WarehouseId warehouseId,
            FCProductStatus status);

    List<FCProduct> findBySku(String sku);

    List<FCProduct> findByWarehouseIdAndVelocity(String warehouseId, VelocityClass velocity);

    boolean existsBySkuAndWarehouseId(String sku, String warehouseId);
}
