package com.coupang.product.infrastructure.persistence.readmodel;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 대시보드/운영팀용 Read Model.
 *
 * FC별 상품 운영 현황을 한눈에 볼 수 있도록 비정규화.
 * 상품 마스터 + FC 운영 정보 + 규칙 평가 결과를 종합.
 */
@Entity
@Table(name = "read_dashboard_product_view", indexes = {
        @Index(name = "idx_read_dash_wh_status", columnList = "warehouseId, status"),
        @Index(name = "idx_read_dash_wh_velocity", columnList = "warehouseId, velocity"),
        @Index(name = "idx_read_dash_wh_temp", columnList = "warehouseId, temperatureZone")
})
public class DashboardProductView {

    @Id
    private String fcProductId;

    private String sku;
    private String productName;
    private String warehouseId;
    private String status;
    private String categoryCode;

    // 보관
    private String temperatureZone;
    private String storageType;

    // 슬로팅
    private String primaryLocation;
    private String velocity;

    // 보충
    private int replenishTriggerPoint;
    private int replenishMaxQty;

    // 규칙 평가 결과 (Rule Engine에서 산출)
    private String slottingZoneType;        // GOLDEN_ZONE, STANDARD, BULK...
    private String slottingReason;          // 규칙 적용 사유

    // 풀필먼트
    private String fulfillmentTypes;        // "ROCKET,ROCKET_FRESH" 쉼표 구분

    // 타임스탬프
    private LocalDateTime registeredAt;
    private LocalDateTime activatedAt;
    private LocalDateTime lastUpdated;

    // Getters & Setters
    public String getFcProductId() { return fcProductId; }
    public void setFcProductId(String fcProductId) { this.fcProductId = fcProductId; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getTemperatureZone() { return temperatureZone; }
    public void setTemperatureZone(String temperatureZone) { this.temperatureZone = temperatureZone; }
    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    public String getPrimaryLocation() { return primaryLocation; }
    public void setPrimaryLocation(String primaryLocation) { this.primaryLocation = primaryLocation; }
    public String getVelocity() { return velocity; }
    public void setVelocity(String velocity) { this.velocity = velocity; }
    public int getReplenishTriggerPoint() { return replenishTriggerPoint; }
    public void setReplenishTriggerPoint(int replenishTriggerPoint) { this.replenishTriggerPoint = replenishTriggerPoint; }
    public int getReplenishMaxQty() { return replenishMaxQty; }
    public void setReplenishMaxQty(int replenishMaxQty) { this.replenishMaxQty = replenishMaxQty; }
    public String getSlottingZoneType() { return slottingZoneType; }
    public void setSlottingZoneType(String slottingZoneType) { this.slottingZoneType = slottingZoneType; }
    public String getSlottingReason() { return slottingReason; }
    public void setSlottingReason(String slottingReason) { this.slottingReason = slottingReason; }
    public String getFulfillmentTypes() { return fulfillmentTypes; }
    public void setFulfillmentTypes(String fulfillmentTypes) { this.fulfillmentTypes = fulfillmentTypes; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { this.activatedAt = activatedAt; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
