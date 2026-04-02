package com.coupang.product.infrastructure.persistence.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DashboardProductViewRepository extends JpaRepository<DashboardProductView, String> {

    List<DashboardProductView> findByWarehouseIdAndStatus(String warehouseId, String status);

    List<DashboardProductView> findByWarehouseIdAndVelocity(String warehouseId, String velocity);

    @Query("SELECT d.velocity, COUNT(d) FROM DashboardProductView d " +
            "WHERE d.warehouseId = :warehouseId GROUP BY d.velocity")
    List<Object[]> countByWarehouseGroupByVelocity(String warehouseId);

    @Query("SELECT d.temperatureZone, COUNT(d) FROM DashboardProductView d " +
            "WHERE d.warehouseId = :warehouseId AND d.status = 'ACTIVE' GROUP BY d.temperatureZone")
    List<Object[]> countActiveByWarehouseGroupByTempZone(String warehouseId);
}
