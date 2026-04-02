package com.coupang.product.infrastructure.persistence.readmodel;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 피킹 시스템용 Read Model.
 *
 * 피킹 Task 생성 시 필요한 정보만 비정규화.
 * FCProduct + SlottingInfo + HandlingRule 을 조합.
 *
 * 복합키: (sku + warehouseId)
 */
@Entity
@Table(name = "read_picking_product_view", indexes = {
        @Index(name = "idx_read_picking_sku_wh", columnList = "sku, warehouseId", unique = true),
        @Index(name = "idx_read_picking_wh_velocity", columnList = "warehouseId, velocity"),
        @Index(name = "idx_read_picking_location", columnList = "primaryLocation")
})
public class PickingProductView {

    @Id
    private String fcProductId;

    private String sku;
    private String warehouseId;
    private String status;
    private boolean operational;

    // 슬로팅 (피킹 경로의 핵심)
    private String primaryLocation;
    private String replenishLocation;
    private String bulkLocation;
    private int pickFaceCapacity;

    // Velocity (피킹 우선순위)
    private String velocity;

    // 취급 규칙 (피킹 시 주의사항)
    private boolean fifo;
    private boolean expiryManaged;
    private boolean fragile;

    // 보충 정책 (보충 Task 생성용)
    private int replenishTriggerPoint;
    private int replenishMaxQty;
    private String replenishUnit;

    // 온도대 (콜드체인 피킹 구분)
    private String temperatureZone;

    // 물리 규격 (피킹 도구 선택)
    private int weightG;
    private boolean oversized;

    private LocalDateTime lastUpdated;

    // Getters & Setters
    public String getFcProductId() { return fcProductId; }
    public void setFcProductId(String fcProductId) { this.fcProductId = fcProductId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }
    public String getPrimaryLocation() { return primaryLocation; }
    public void setPrimaryLocation(String primaryLocation) { this.primaryLocation = primaryLocation; }
    public String getReplenishLocation() { return replenishLocation; }
    public void setReplenishLocation(String replenishLocation) { this.replenishLocation = replenishLocation; }
    public String getBulkLocation() { return bulkLocation; }
    public void setBulkLocation(String bulkLocation) { this.bulkLocation = bulkLocation; }
    public int getPickFaceCapacity() { return pickFaceCapacity; }
    public void setPickFaceCapacity(int pickFaceCapacity) { this.pickFaceCapacity = pickFaceCapacity; }
    public String getVelocity() { return velocity; }
    public void setVelocity(String velocity) { this.velocity = velocity; }
    public boolean isFifo() { return fifo; }
    public void setFifo(boolean fifo) { this.fifo = fifo; }
    public boolean isExpiryManaged() { return expiryManaged; }
    public void setExpiryManaged(boolean expiryManaged) { this.expiryManaged = expiryManaged; }
    public boolean isFragile() { return fragile; }
    public void setFragile(boolean fragile) { this.fragile = fragile; }
    public int getReplenishTriggerPoint() { return replenishTriggerPoint; }
    public void setReplenishTriggerPoint(int replenishTriggerPoint) { this.replenishTriggerPoint = replenishTriggerPoint; }
    public int getReplenishMaxQty() { return replenishMaxQty; }
    public void setReplenishMaxQty(int replenishMaxQty) { this.replenishMaxQty = replenishMaxQty; }
    public String getReplenishUnit() { return replenishUnit; }
    public void setReplenishUnit(String replenishUnit) { this.replenishUnit = replenishUnit; }
    public String getTemperatureZone() { return temperatureZone; }
    public void setTemperatureZone(String temperatureZone) { this.temperatureZone = temperatureZone; }
    public int getWeightG() { return weightG; }
    public void setWeightG(int weightG) { this.weightG = weightG; }
    public boolean isOversized() { return oversized; }
    public void setOversized(boolean oversized) { this.oversized = oversized; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
