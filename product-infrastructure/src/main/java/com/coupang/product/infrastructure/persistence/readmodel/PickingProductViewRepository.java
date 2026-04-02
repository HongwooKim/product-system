package com.coupang.product.infrastructure.persistence.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PickingProductViewRepository extends JpaRepository<PickingProductView, String> {

    Optional<PickingProductView> findBySkuAndWarehouseId(String sku, String warehouseId);

    List<PickingProductView> findByWarehouseIdAndOperationalTrue(String warehouseId);

    List<PickingProductView> findByWarehouseIdAndVelocity(String warehouseId, String velocity);

    List<PickingProductView> findByPrimaryLocation(String primaryLocation);
}
