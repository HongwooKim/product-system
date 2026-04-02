package com.coupang.product.domain.event;

import com.coupang.product.domain.model.common.DomainEvent;
import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.ProductDimension;
import com.coupang.product.domain.model.product.SKU;

import java.util.Set;

/**
 * FCProduct Aggregate의 도메인 이벤트.
 */
public final class FCProductEvents {
    private FCProductEvents() {}

    public static abstract class Base extends DomainEvent {
        private final FCProductId fcProductId;
        private final WarehouseId warehouseId;
        private final SKU sku;

        protected Base(FCProductId fcProductId, WarehouseId warehouseId, SKU sku) {
            super();
            this.fcProductId = fcProductId;
            this.warehouseId = warehouseId;
            this.sku = sku;
        }

        public FCProductId getFcProductId() { return fcProductId; }
        public WarehouseId getWarehouseId() { return warehouseId; }
        public SKU getSku() { return sku; }
    }

    /** 소비자: 입고(입고 가능 등록), 재고(FC별 SKU 등록) */
    public static class Registered extends Base {
        private final StorageProfile storageProfile;
        private final HandlingRule handlingRule;
        private final Set<FulfillmentType> fulfillmentTypes;
        private final VelocityClass velocity;
        private final ProductDimension dimensions;

        public Registered(FCProductId fcProductId, WarehouseId warehouseId, SKU sku,
                          StorageProfile storageProfile, HandlingRule handlingRule,
                          Set<FulfillmentType> fulfillmentTypes,
                          VelocityClass velocity, ProductDimension dimensions) {
            super(fcProductId, warehouseId, sku);
            this.storageProfile = storageProfile;
            this.handlingRule = handlingRule;
            this.fulfillmentTypes = fulfillmentTypes;
            this.velocity = velocity;
            this.dimensions = dimensions;
        }

        @Override public String getEventType() { return "fcproduct.registered"; }
        public StorageProfile getStorageProfile() { return storageProfile; }
        public HandlingRule getHandlingRule() { return handlingRule; }
        public Set<FulfillmentType> getFulfillmentTypes() { return fulfillmentTypes; }
        public VelocityClass getVelocity() { return velocity; }
        public ProductDimension getDimensions() { return dimensions; }
    }

    /** 소비자: 재고(적치위치), 피킹(지번), 입고(안내) */
    public static class SlottingAssigned extends Base {
        private final SlottingInfo slottingInfo;
        private final StorageProfile storageProfile;
        private final HandlingRule handlingRule;
        private final VelocityClass velocity;
        private final ReplenishmentPolicy replenishmentPolicy;
        private final ProductDimension dimensions;

        public SlottingAssigned(FCProductId fcProductId, WarehouseId warehouseId, SKU sku,
                                SlottingInfo slottingInfo, StorageProfile storageProfile,
                                HandlingRule handlingRule, VelocityClass velocity,
                                ReplenishmentPolicy replenishmentPolicy, ProductDimension dimensions) {
            super(fcProductId, warehouseId, sku);
            this.slottingInfo = slottingInfo;
            this.storageProfile = storageProfile;
            this.handlingRule = handlingRule;
            this.velocity = velocity;
            this.replenishmentPolicy = replenishmentPolicy;
            this.dimensions = dimensions;
        }

        @Override public String getEventType() { return "fcproduct.slotting-assigned"; }
        public SlottingInfo getSlottingInfo() { return slottingInfo; }
        public StorageProfile getStorageProfile() { return storageProfile; }
        public HandlingRule getHandlingRule() { return handlingRule; }
        public VelocityClass getVelocity() { return velocity; }
        public ReplenishmentPolicy getReplenishmentPolicy() { return replenishmentPolicy; }
        public ProductDimension getDimensions() { return dimensions; }
    }

    /** 소비자: 재고(위치변경), 피킹(지번갱신) */
    public static class Reslotted extends Base {
        private final SlottingInfo previousSlotting;
        private final SlottingInfo newSlotting;
        private final String reason;

        public Reslotted(FCProductId fcProductId, WarehouseId warehouseId, SKU sku,
                         SlottingInfo previousSlotting, SlottingInfo newSlotting, String reason) {
            super(fcProductId, warehouseId, sku);
            this.previousSlotting = previousSlotting;
            this.newSlotting = newSlotting;
            this.reason = reason;
        }

        @Override public String getEventType() { return "fcproduct.reslotted"; }
        public SlottingInfo getPreviousSlotting() { return previousSlotting; }
        public SlottingInfo getNewSlotting() { return newSlotting; }
        public String getReason() { return reason; }
    }

    /** 소비자: 피킹(우선순위갱신), 재슬로팅 배치 */
    public static class VelocityReclassified extends Base {
        private final VelocityClass previousVelocity;
        private final VelocityClass newVelocity;
        private final ReplenishmentPolicy replenishmentPolicy;

        public VelocityReclassified(FCProductId fcProductId, WarehouseId warehouseId, SKU sku,
                                     VelocityClass previousVelocity, VelocityClass newVelocity,
                                     ReplenishmentPolicy replenishmentPolicy) {
            super(fcProductId, warehouseId, sku);
            this.previousVelocity = previousVelocity;
            this.newVelocity = newVelocity;
            this.replenishmentPolicy = replenishmentPolicy;
        }

        @Override public String getEventType() { return "fcproduct.velocity-reclassified"; }
        public VelocityClass getPreviousVelocity() { return previousVelocity; }
        public VelocityClass getNewVelocity() { return newVelocity; }
        public ReplenishmentPolicy getReplenishmentPolicy() { return replenishmentPolicy; }
    }

    /** 소비자: 입고(거부), 피킹(제외), 출고(보류), 주문(판매불가) */
    public static class Suspended extends Base {
        private final SuspensionReason reason;

        public Suspended(FCProductId fcProductId, WarehouseId warehouseId, SKU sku,
                         SuspensionReason reason) {
            super(fcProductId, warehouseId, sku);
            this.reason = reason;
        }

        @Override public String getEventType() { return "fcproduct.suspended"; }
        public SuspensionReason getReason() { return reason; }
    }

    /** 소비자: 입고(거부), 재고(잔여이관/폐기), 피킹(제외) */
    public static class Discontinued extends Base {
        public Discontinued(FCProductId fcProductId, WarehouseId warehouseId, SKU sku) {
            super(fcProductId, warehouseId, sku);
        }
        @Override public String getEventType() { return "fcproduct.discontinued"; }
    }
}
