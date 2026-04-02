package com.coupang.product.domain.port.readmodel;

public record DashboardProductViewDto(
        String fcProductId,
        String sku,
        String productName,
        String warehouseId,
        String status,
        String categoryCode,
        String temperatureZone,
        String storageType,
        String primaryLocation,
        String velocity,
        int replenishTriggerPoint,
        int replenishMaxQty,
        String slottingZoneType,
        String slottingReason,
        String fulfillmentTypes,
        String registeredAt,
        String activatedAt,
        String lastUpdated
) {}
