package com.coupang.product.application.query.readmodel;

public record PickingProductViewDto(
        String fcProductId,
        String sku,
        String warehouseId,
        String status,
        boolean operational,
        String primaryLocation,
        String replenishLocation,
        String bulkLocation,
        int pickFaceCapacity,
        String velocity,
        boolean fifo,
        boolean expiryManaged,
        boolean fragile,
        int replenishTriggerPoint,
        int replenishMaxQty,
        String replenishUnit,
        String temperatureZone,
        int weightG,
        boolean oversized,
        String lastUpdated
) {}
