package com.coupang.product.domain.model.fcproduct;

import com.coupang.product.domain.event.FCProductEvents;
import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.common.DomainEvent;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.ProductDimension;
import com.coupang.product.domain.model.product.SKU;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FCProductTest {

    // ═══════════════════════════════════════
    //  Helper methods
    // ═══════════════════════════════════════

    private static SKU sku(String value) {
        return new SKU(value);
    }

    private static WarehouseId warehouseId() {
        return new WarehouseId("FC-PYEONGTAEK");
    }

    private static ProductDimension dimensions() {
        return new ProductDimension(300, 200, 100, 500);
    }

    private static StorageProfile ambientStorage() {
        return new StorageProfile(TemperatureZone.AMBIENT, StorageType.SHELF_RACK, 3, null);
    }

    private static StorageProfile coldStorage() {
        return new StorageProfile(TemperatureZone.CHILLED, StorageType.SHELF_RACK, 3, null);
    }

    private static HandlingRule standardHandling() {
        return HandlingRule.standard();
    }

    private static HandlingRule freshHandling() {
        return HandlingRule.forRocketFresh();
    }

    private static Set<FulfillmentType> rocketFulfillment() {
        return Set.of(FulfillmentType.ROCKET);
    }

    private static Set<FulfillmentType> freshFulfillment() {
        return Set.of(FulfillmentType.ROCKET_FRESH);
    }

    private static FCProduct registerStandardFCProduct() {
        return FCProduct.register(
                sku("SKU-001"), warehouseId(), dimensions(),
                ambientStorage(), standardHandling(), rocketFulfillment()
        );
    }

    private static SlottingInfo slottingInfo() {
        return new SlottingInfo(
                new LocationCode("A-12-03-2-B"),
                new LocationCode("A-12-03-3-B"),
                new LocationCode("B-01-01-1-A"),
                20
        );
    }

    private static FCProduct activeFCProduct() {
        FCProduct fp = registerStandardFCProduct();
        fp.assignSlotting(slottingInfo());
        fp.clearDomainEvents();
        return fp;
    }

    // ═══════════════════════════════════════
    //  register()
    // ═══════════════════════════════════════

    @Test
    void register_createsWithPendingSetupStatusAndVelocityD() {
        FCProduct fp = registerStandardFCProduct();

        assertEquals(FCProductStatus.PENDING_SETUP, fp.getStatus());
        assertEquals(VelocityClass.D, fp.getVelocity());
        assertNotNull(fp.getId());
        assertNotNull(fp.getRegisteredAt());
    }

    @Test
    void register_rocketFreshWithoutFifo_throwsException() {
        HandlingRule noFifo = new HandlingRule(false, true, true, false);

        DomainException ex = assertThrows(DomainException.class, () ->
                FCProduct.register(
                        sku("SKU-FRESH"), warehouseId(), dimensions(),
                        coldStorage(), noFifo, freshFulfillment()
                ));
        assertEquals("ROCKET_FRESH_CONSTRAINT", ex.getCode());
    }

    @Test
    void register_rocketFreshWithoutColdChain_throwsException() {
        DomainException ex = assertThrows(DomainException.class, () ->
                FCProduct.register(
                        sku("SKU-FRESH"), warehouseId(), dimensions(),
                        ambientStorage(), freshHandling(), freshFulfillment()
                ));
        assertEquals("ROCKET_FRESH_TEMP", ex.getCode());
    }

    // ═══════════════════════════════════════
    //  assignSlotting()
    // ═══════════════════════════════════════

    @Test
    void assignSlotting_transitionsToActive() {
        FCProduct fp = registerStandardFCProduct();

        fp.assignSlotting(slottingInfo());

        assertEquals(FCProductStatus.ACTIVE, fp.getStatus());
        assertNotNull(fp.getSlottingInfo());
        assertNotNull(fp.getActivatedAt());
    }

    // ═══════════════════════════════════════
    //  reclassifyVelocity()
    // ═══════════════════════════════════════

    @Test
    void reclassifyVelocity_withSameVelocity_doesNothing() {
        FCProduct fp = activeFCProduct();
        int eventsBefore = fp.getDomainEvents().size();

        fp.reclassifyVelocity(VelocityClass.D); // same as initial

        assertEquals(eventsBefore, fp.getDomainEvents().size());
        assertTrue(fp.getVelocityHistory().isEmpty());
    }

    @Test
    void reclassifyVelocity_withDifferentVelocity_raisesEvent() {
        FCProduct fp = activeFCProduct();

        fp.reclassifyVelocity(VelocityClass.A);

        assertEquals(VelocityClass.A, fp.getVelocity());
        assertFalse(fp.getVelocityHistory().isEmpty());

        List<DomainEvent> events = fp.getDomainEvents();
        boolean hasVelocityEvent = events.stream()
                .anyMatch(e -> e instanceof FCProductEvents.VelocityReclassified);
        assertTrue(hasVelocityEvent);

        FCProductEvents.VelocityReclassified event = events.stream()
                .filter(e -> e instanceof FCProductEvents.VelocityReclassified)
                .map(e -> (FCProductEvents.VelocityReclassified) e)
                .findFirst().orElseThrow();
        assertEquals(VelocityClass.D, event.getPreviousVelocity());
        assertEquals(VelocityClass.A, event.getNewVelocity());
    }

    // ═══════════════════════════════════════
    //  needsReplenishment()
    // ═══════════════════════════════════════

    @Test
    void needsReplenishment_returnsTrueWhenBelowTrigger() {
        FCProduct fp = activeFCProduct();
        // Default D velocity: triggerPoint=3
        assertTrue(fp.needsReplenishment(2));
        assertTrue(fp.needsReplenishment(3)); // equal to trigger also triggers
    }

    @Test
    void needsReplenishment_returnsFalseWhenAboveTrigger() {
        FCProduct fp = activeFCProduct();
        // Default D velocity: triggerPoint=3
        assertFalse(fp.needsReplenishment(4));
        assertFalse(fp.needsReplenishment(10));
    }

    // ═══════════════════════════════════════
    //  suspend()
    // ═══════════════════════════════════════

    @Test
    void suspend_onActiveProduct_works() {
        FCProduct fp = activeFCProduct();

        fp.suspend(SuspensionReason.QUALITY_ISSUE);

        assertEquals(FCProductStatus.SUSPENDED, fp.getStatus());
        assertEquals(SuspensionReason.QUALITY_ISSUE, fp.getSuspensionReason());
    }

    @Test
    void suspend_onPendingSetup_throwsException() {
        FCProduct fp = registerStandardFCProduct();

        DomainException ex = assertThrows(DomainException.class,
                () -> fp.suspend(SuspensionReason.QUALITY_ISSUE));
        assertEquals("INVALID_STATUS_TRANSITION", ex.getCode());
    }

    // ═══════════════════════════════════════
    //  reconstitute()
    // ═══════════════════════════════════════

    @Test
    void reconstitute_doesNotRaiseEvents() {
        FCProduct fp = FCProduct.reconstitute(
                FCProductId.generate(), sku("SKU-RECON"), warehouseId(),
                FCProductStatus.ACTIVE,
                ambientStorage(), standardHandling(),
                slottingInfo(), VelocityClass.B,
                ReplenishmentPolicy.defaultFor(VelocityClass.B),
                dimensions(), rocketFulfillment(),
                null,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3), null
        );

        assertTrue(fp.getDomainEvents().isEmpty());
    }
}
