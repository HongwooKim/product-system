package com.coupang.product.interfaces.dto;

import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.FulfillmentType;

import java.util.Set;

public record FCProductResponse(
        String fcProductId,
        String sku,
        String warehouseId,
        String status,
        StorageProfileDto storageProfile,
        HandlingRuleDto handlingRule,
        SlottingInfoDto slottingInfo,
        String velocity,
        ReplenishmentPolicyDto replenishmentPolicy,
        Set<FulfillmentType> fulfillmentTypes,
        String suspensionReason,
        String registeredAt,
        String activatedAt
) {
    public static FCProductResponse from(FCProduct fp) {
        return new FCProductResponse(
                fp.getId().value(),
                fp.getSku().value(),
                fp.getWarehouseId().value(),
                fp.getStatus().name(),
                new StorageProfileDto(
                        fp.getStorageProfile().temperatureZone().name(),
                        fp.getStorageProfile().temperatureZone().getDisplayName(),
                        fp.getStorageProfile().storageType().name(),
                        fp.getStorageProfile().maxStackHeight()
                ),
                new HandlingRuleDto(
                        fp.getHandlingRule().fifo(),
                        fp.getHandlingRule().expiryManaged(),
                        fp.getHandlingRule().lotTracking(),
                        fp.getHandlingRule().fragile()
                ),
                fp.getSlottingInfo() != null ? new SlottingInfoDto(
                        fp.getSlottingInfo().primaryLocation().value(),
                        fp.getSlottingInfo().replenishLocation().value(),
                        fp.getSlottingInfo().bulkLocation() != null ?
                                fp.getSlottingInfo().bulkLocation().value() : null,
                        fp.getSlottingInfo().pickFaceCapacity()
                ) : null,
                fp.getVelocity().name(),
                new ReplenishmentPolicyDto(
                        fp.getReplenishmentPolicy().minQty(),
                        fp.getReplenishmentPolicy().maxQty(),
                        fp.getReplenishmentPolicy().triggerPoint(),
                        fp.getReplenishmentPolicy().replenishUnit().name()
                ),
                fp.getFulfillmentTypes(),
                fp.getSuspensionReason() != null ? fp.getSuspensionReason().name() : null,
                fp.getRegisteredAt() != null ? fp.getRegisteredAt().toString() : null,
                fp.getActivatedAt() != null ? fp.getActivatedAt().toString() : null
        );
    }

    public record StorageProfileDto(String temperatureZone, String temperatureZoneDisplayName,
                                     String storageType, int maxStackHeight) {}
    public record HandlingRuleDto(boolean fifo, boolean expiryManaged, boolean lotTracking, boolean fragile) {}
    public record SlottingInfoDto(String primaryLocation, String replenishLocation,
                                   String bulkLocation, int pickFaceCapacity) {}
    public record ReplenishmentPolicyDto(int minQty, int maxQty, int triggerPoint, String replenishUnit) {}
}
