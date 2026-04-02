package com.coupang.product.infrastructure.persistence.repository;

import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.model.fcproduct.FCProductId;
import com.coupang.product.domain.model.fcproduct.FCProductStatus;
import com.coupang.product.domain.model.fcproduct.VelocityClass;
import com.coupang.product.domain.model.fcproduct.WarehouseId;
import com.coupang.product.domain.model.product.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FCProductJpaRepository extends JpaRepository<FCProduct, FCProductId> {

    @Query("SELECT fp FROM FCProduct fp WHERE fp.sku = :sku AND fp.warehouseId = :warehouseId")
    Optional<FCProduct> findBySkuAndWarehouseId(@Param("sku") SKU sku, @Param("warehouseId") WarehouseId warehouseId);

    @Query("SELECT fp FROM FCProduct fp WHERE fp.warehouseId = :warehouseId AND fp.status = :status")
    List<FCProduct> findByWarehouseIdAndStatus(@Param("warehouseId") WarehouseId warehouseId, @Param("status") FCProductStatus status);

    @Query("SELECT fp FROM FCProduct fp WHERE fp.sku = :sku")
    List<FCProduct> findBySku(@Param("sku") SKU sku);

    @Query("SELECT fp FROM FCProduct fp WHERE fp.warehouseId = :warehouseId AND fp.velocity = :velocity")
    List<FCProduct> findByWarehouseIdAndVelocity(@Param("warehouseId") WarehouseId warehouseId, @Param("velocity") VelocityClass velocity);

    @Query("SELECT CASE WHEN COUNT(fp) > 0 THEN true ELSE false END FROM FCProduct fp WHERE fp.sku = :sku AND fp.warehouseId = :warehouseId")
    boolean existsBySkuAndWarehouseId(@Param("sku") SKU sku, @Param("warehouseId") WarehouseId warehouseId);
}
