package com.coupang.product.domain.model.fcproduct;

import com.coupang.product.domain.event.*;
import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.common.AggregateRoot;
import com.coupang.product.domain.model.converter.*;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.ProductDimension;
import com.coupang.product.domain.model.product.SKU;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * FC 상품 Aggregate Root.
 *
 * "이 상품이 이 FC에서 어떻게 운영되는가"를 정의한다.
 * 같은 SKU라도 FC마다 별도의 FCProduct가 존재한다.
 *
 * 소유하는 결정권:
 * - 보관 프로파일 (온도대, 보관 방식)
 * - 취급 규칙 (FIFO, 유통기한, 로트, 취급주의)
 * - 슬로팅 (피킹/보충/벌크 지번)
 * - 보충 정책
 * - 출고빈도 등급 (Velocity)
 *
 * 소유하지 않는 것:
 * - 상품 자체 정보 → Product Aggregate
 * - 실물 재고 수량 → 재고 시스템
 * - 피킹/패킹 실행 → 피킹 시스템
 */
@Entity
@Table(name = "fc_products", indexes = {
        @Index(name = "idx_fcproduct_sku_warehouse", columnList = "sku, warehouseId", unique = true),
        @Index(name = "idx_fcproduct_warehouse_status", columnList = "warehouseId, status"),
        @Index(name = "idx_fcproduct_warehouse_velocity", columnList = "warehouseId, velocity")
})
public class FCProduct extends AggregateRoot {

    @Id
    @Column(length = 36)
    @Convert(converter = FCProductIdConverter.class)
    private FCProductId id;

    @Column(nullable = false, length = 50)
    @Convert(converter = SKUConverter.class)
    private SKU sku;

    @Column(nullable = false, length = 50)
    @Convert(converter = WarehouseIdConverter.class)
    private WarehouseId warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FCProductStatus status;

    // Storage Profile (flat columns)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TemperatureZone temperatureZone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StorageType storageType;

    private int maxStackHeight;

    @Column(length = 20)
    private String hazardClass;

    // Handling Rule (flat columns)
    private boolean fifo;
    private boolean expiryManaged;
    private boolean lotTracking;
    private boolean fragile;

    // Slotting (flat columns, nullable — ACTIVE 전에는 null)
    @Column(length = 30)
    @Convert(converter = LocationCodeConverter.class)
    private LocationCode primaryLocation;

    @Column(length = 30)
    @Convert(converter = LocationCodeConverter.class)
    private LocationCode replenishLocation;

    @Column(length = 30)
    @Convert(converter = LocationCodeConverter.class)
    private LocationCode bulkLocation;

    private int pickFaceCapacity;

    // Velocity
    @Enumerated(EnumType.STRING)
    @Column(length = 5)
    private VelocityClass velocity;

    @Transient
    private List<VelocityChange> velocityHistory;

    // Replenishment Policy (flat columns)
    private int replenishMinQty;
    private int replenishMaxQty;
    private int replenishTriggerPoint;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ReplenishmentPolicy.ReplenishUnit replenishUnit;

    // Dimensions (Product 복제본, flat columns)
    private int lengthMm;
    private int widthMm;
    private int heightMm;
    private int weightG;

    // Fulfillment Types
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "fc_product_fulfillment_types", joinColumns = @JoinColumn(name = "fc_product_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type")
    private Set<FulfillmentType> fulfillmentTypes;

    // Suspension
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SuspensionReason suspensionReason;

    // Timestamps
    private LocalDateTime registeredAt;
    private LocalDateTime activatedAt;
    private LocalDateTime suspendedAt;

    // ═══════════════════════════════════════
    //  Factory Method
    // ═══════════════════════════════════════

    /**
     * 신규 FC 상품 등록.
     * 입고 시스템의 ASN 수신 또는 수동 등록으로 트리거된다.
     */
    public static FCProduct register(
            SKU sku,
            WarehouseId warehouseId,
            ProductDimension dimensions,
            StorageProfile storageProfile,
            HandlingRule handlingRule,
            Set<FulfillmentType> fulfillmentTypes
    ) {
        FCProduct fp = new FCProduct();
        fp.id = FCProductId.generate();
        fp.sku = Objects.requireNonNull(sku);
        fp.warehouseId = Objects.requireNonNull(warehouseId);
        fp.setDimensionsInternal(Objects.requireNonNull(dimensions));
        fp.setStorageProfileInternal(Objects.requireNonNull(storageProfile));
        fp.setHandlingRuleInternal(Objects.requireNonNull(handlingRule));
        fp.fulfillmentTypes = new HashSet<>(fulfillmentTypes);
        fp.status = FCProductStatus.PENDING_SETUP;
        fp.velocity = VelocityClass.D;  // 신규는 최저 등급
        fp.velocityHistory = new ArrayList<>();
        fp.setReplenishmentPolicyInternal(ReplenishmentPolicy.defaultFor(VelocityClass.D));
        fp.registeredAt = LocalDateTime.now();

        fp.enforceHandlingRuleConstraints();

        fp.raise(new FCProductEvents.Registered(
                fp.id, fp.warehouseId, fp.sku,
                fp.getStorageProfile(), fp.getHandlingRule(), fp.fulfillmentTypes,
                fp.velocity, fp.getDimensions()
        ));

        return fp;
    }

    /**
     * DB에서 복원할 때 사용. 이벤트 발행 없음, 검증 없음.
     */
    public static FCProduct reconstitute(
            FCProductId id, SKU sku, WarehouseId warehouseId, FCProductStatus status,
            StorageProfile storageProfile, HandlingRule handlingRule,
            SlottingInfo slottingInfo, VelocityClass velocity,
            ReplenishmentPolicy replenishmentPolicy, ProductDimension dimensions,
            Set<FulfillmentType> fulfillmentTypes, SuspensionReason suspensionReason,
            LocalDateTime registeredAt, LocalDateTime activatedAt, LocalDateTime suspendedAt
    ) {
        FCProduct fp = new FCProduct();
        fp.id = id;
        fp.sku = sku;
        fp.warehouseId = warehouseId;
        fp.status = status;
        fp.setStorageProfileInternal(storageProfile);
        fp.setHandlingRuleInternal(handlingRule);
        fp.setSlottingInfoInternal(slottingInfo);
        fp.velocity = velocity;
        fp.velocityHistory = new ArrayList<>();
        fp.setReplenishmentPolicyInternal(replenishmentPolicy);
        fp.setDimensionsInternal(dimensions);
        fp.fulfillmentTypes = new HashSet<>(fulfillmentTypes);
        fp.suspensionReason = suspensionReason;
        fp.registeredAt = registeredAt;
        fp.activatedAt = activatedAt;
        fp.suspendedAt = suspendedAt;
        return fp;
    }

    protected FCProduct() {
        this.velocityHistory = new ArrayList<>();
    }

    // ═══════════════════════════════════════
    //  슬로팅
    // ═══════════════════════════════════════

    /**
     * 슬로팅 프로세스 시작. PENDING_SETUP → SLOTTING.
     */
    public void startSlotting() {
        assertTransition(FCProductStatus.SLOTTING);
        this.status = FCProductStatus.SLOTTING;
    }

    /**
     * 지번 배정 완료. SLOTTING → ACTIVE.
     * 슬로팅 정보의 온도대/보관타입이 storageProfile과 호환되어야 한다.
     */
    public void assignSlotting(SlottingInfo newSlotting) {
        if (status != FCProductStatus.SLOTTING && status != FCProductStatus.PENDING_SETUP) {
            throw new DomainException("INVALID_STATUS",
                    "슬로팅은 PENDING_SETUP 또는 SLOTTING 상태에서만 가능합니다: " + status);
        }

        setSlottingInfoInternal(Objects.requireNonNull(newSlotting));
        this.status = FCProductStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();

        raise(new FCProductEvents.SlottingAssigned(
                id, warehouseId, sku, getSlottingInfo(), getStorageProfile(),
                getHandlingRule(), velocity, getReplenishmentPolicy(), getDimensions()
        ));
    }

    /**
     * 재슬로팅. velocity 변동 또는 운영 판단에 의해 지번 변경.
     */
    public void reslot(SlottingInfo newSlotting, String reason) {
        assertOperational();
        SlottingInfo previous = getSlottingInfo();
        setSlottingInfoInternal(Objects.requireNonNull(newSlotting));

        raise(new FCProductEvents.Reslotted(id, warehouseId, sku, previous, newSlotting, reason));
    }

    // ═══════════════════════════════════════
    //  Velocity
    // ═══════════════════════════════════════

    /**
     * 출고빈도 등급 재산정.
     * 피킹 시스템에서 출고 실적 데이터를 받아 일배치로 실행.
     */
    public void reclassifyVelocity(VelocityClass newVelocity) {
        if (this.velocity == newVelocity) return;

        VelocityClass previous = this.velocity;
        this.velocity = newVelocity;
        this.velocityHistory.add(new VelocityChange(previous, newVelocity, LocalDateTime.now()));

        // 보충 정책도 자동 조정
        setReplenishmentPolicyInternal(ReplenishmentPolicy.defaultFor(newVelocity));

        raise(new FCProductEvents.VelocityReclassified(
                id, warehouseId, sku, previous, newVelocity, getReplenishmentPolicy()
        ));
    }

    public boolean hasRecentVelocityChange(int withinDays) {
        if (velocityHistory.isEmpty()) return false;
        VelocityChange last = velocityHistory.getLast();
        return last.changedAt().isAfter(LocalDateTime.now().minusDays(withinDays));
    }

    // ═══════════════════════════════════════
    //  보충
    // ═══════════════════════════════════════

    public void updateReplenishmentPolicy(ReplenishmentPolicy policy) {
        setReplenishmentPolicyInternal(Objects.requireNonNull(policy));
    }

    /**
     * 보충 필요 여부 판단.
     * 재고 시스템에서 피킹 지번의 현재 수량을 전달받아 호출.
     */
    public boolean needsReplenishment(int currentPickFaceQty) {
        assertOperational();
        return getReplenishmentPolicy().isTriggered(currentPickFaceQty);
    }

    /**
     * 보충 스펙 생성. 피킹 시스템에 전달할 보충 작업 정보.
     */
    public ReplenishmentSpec buildReplenishmentSpec(int currentPickFaceQty) {
        assertOperational();
        ReplenishmentPolicy policy = getReplenishmentPolicy();
        int replenishQty = policy.calculateReplenishQty(currentPickFaceQty);

        return new ReplenishmentSpec(
                id, sku, warehouseId,
                replenishLocation,
                primaryLocation,
                replenishQty,
                policy.replenishUnit(),
                fifo
        );
    }

    // ═══════════════════════════════════════
    //  생명주기 전이
    // ═══════════════════════════════════════

    public void suspend(SuspensionReason reason) {
        assertTransition(FCProductStatus.SUSPENDED);
        this.status = FCProductStatus.SUSPENDED;
        this.suspensionReason = reason;
        this.suspendedAt = LocalDateTime.now();

        raise(new FCProductEvents.Suspended(id, warehouseId, sku, reason));
    }

    public void reactivate() {
        assertTransition(FCProductStatus.ACTIVE);
        this.status = FCProductStatus.ACTIVE;
        this.suspensionReason = null;
    }

    public void discontinue() {
        if (status == FCProductStatus.DISCONTINUED) return;
        assertTransition(FCProductStatus.DISCONTINUED);
        this.status = FCProductStatus.DISCONTINUED;

        raise(new FCProductEvents.Discontinued(id, warehouseId, sku));
    }

    // ═══════════════════════════════════════
    //  불변식 검증
    // ═══════════════════════════════════════

    private void enforceHandlingRuleConstraints() {
        if (fulfillmentTypes.contains(FulfillmentType.ROCKET_FRESH)) {
            if (!fifo || !expiryManaged) {
                throw new DomainException("ROCKET_FRESH_CONSTRAINT",
                        "로켓프레시 상품은 FIFO와 유통기한 관리가 필수입니다");
            }
            if (!temperatureZone.isColdChain()) {
                throw new DomainException("ROCKET_FRESH_TEMP",
                        "로켓프레시 상품은 냉장 또는 냉동 온도대만 가능합니다");
            }
        }
    }

    private void assertOperational() {
        if (!status.isOperational()) {
            throw new DomainException("NOT_OPERATIONAL",
                    "FCProduct가 운영 상태가 아닙니다: " + status);
        }
    }

    private void assertTransition(FCProductStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "상태 전이 불가: " + status + " → " + target);
        }
    }

    // ═══════════════════════════════════════
    //  VO ↔ flat column helpers
    // ═══════════════════════════════════════

    private void setStorageProfileInternal(StorageProfile sp) {
        this.temperatureZone = sp.temperatureZone();
        this.storageType = sp.storageType();
        this.maxStackHeight = sp.maxStackHeight();
        this.hazardClass = sp.hazardClass();
    }

    private void setHandlingRuleInternal(HandlingRule hr) {
        this.fifo = hr.fifo();
        this.expiryManaged = hr.expiryManaged();
        this.lotTracking = hr.lotTracking();
        this.fragile = hr.fragile();
    }

    private void setSlottingInfoInternal(SlottingInfo si) {
        if (si != null) {
            this.primaryLocation = si.primaryLocation();
            this.replenishLocation = si.replenishLocation();
            this.bulkLocation = si.bulkLocation();
            this.pickFaceCapacity = si.pickFaceCapacity();
        }
    }

    private void setReplenishmentPolicyInternal(ReplenishmentPolicy rp) {
        this.replenishMinQty = rp.minQty();
        this.replenishMaxQty = rp.maxQty();
        this.replenishTriggerPoint = rp.triggerPoint();
        this.replenishUnit = rp.replenishUnit();
    }

    private void setDimensionsInternal(ProductDimension d) {
        this.lengthMm = d.lengthMm();
        this.widthMm = d.widthMm();
        this.heightMm = d.heightMm();
        this.weightG = d.weightG();
    }

    // ═══════════════════════════════════════
    //  Getters
    // ═══════════════════════════════════════

    public FCProductId getId() { return id; }
    public SKU getSku() { return sku; }
    public WarehouseId getWarehouseId() { return warehouseId; }
    public FCProductStatus getStatus() { return status; }
    public StorageProfile getStorageProfile() { return new StorageProfile(temperatureZone, storageType, maxStackHeight, hazardClass); }
    public HandlingRule getHandlingRule() { return new HandlingRule(fifo, expiryManaged, lotTracking, fragile); }
    public SlottingInfo getSlottingInfo() {
        return primaryLocation != null
                ? new SlottingInfo(primaryLocation, replenishLocation, bulkLocation, pickFaceCapacity)
                : null;
    }
    public VelocityClass getVelocity() { return velocity; }
    public List<VelocityChange> getVelocityHistory() { return Collections.unmodifiableList(velocityHistory); }
    public ReplenishmentPolicy getReplenishmentPolicy() { return new ReplenishmentPolicy(replenishMinQty, replenishMaxQty, replenishTriggerPoint, replenishUnit); }
    public ProductDimension getDimensions() { return new ProductDimension(lengthMm, widthMm, heightMm, weightG); }
    public Set<FulfillmentType> getFulfillmentTypes() { return Collections.unmodifiableSet(fulfillmentTypes); }
    public SuspensionReason getSuspensionReason() { return suspensionReason; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public LocalDateTime getActivatedAt() { return activatedAt; }

    // ── 내부 record ──

    public record VelocityChange(VelocityClass from, VelocityClass to, LocalDateTime changedAt) {}

    public record ReplenishmentSpec(
            FCProductId fcProductId, SKU sku, WarehouseId warehouseId,
            LocationCode fromLocation, LocationCode toLocation,
            int qty, ReplenishmentPolicy.ReplenishUnit unit, boolean fifo
    ) {}
}
