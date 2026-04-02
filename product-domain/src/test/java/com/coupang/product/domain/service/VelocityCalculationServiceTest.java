package com.coupang.product.domain.service;

import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.ProductDimension;
import com.coupang.product.domain.model.product.SKU;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class VelocityCalculationServiceTest {

    private VelocityCalculationService service;

    @BeforeEach
    void setUp() {
        service = new VelocityCalculationService();
    }

    // ═══════════════════════════════════════
    //  Helper methods
    // ═══════════════════════════════════════

    private static FCProduct createActiveFCProduct(String skuValue) {
        FCProduct fp = FCProduct.register(
                new SKU(skuValue),
                new WarehouseId("FC-TEST"),
                new ProductDimension(300, 200, 100, 500),
                new StorageProfile(TemperatureZone.AMBIENT, StorageType.SHELF_RACK, 3, null),
                HandlingRule.standard(),
                Set.of(FulfillmentType.ROCKET)
        );
        fp.assignSlotting(new SlottingInfo(
                new LocationCode("A-01-01-1-A"),
                new LocationCode("A-01-01-2-A"),
                null,
                20
        ));
        fp.clearDomainEvents();
        return fp;
    }

    // ═══════════════════════════════════════
    //  recalculateAll()
    // ═══════════════════════════════════════

    @Test
    void recalculateAll_assignsAToTop20Percent() {
        // Create 10 products so percentile boundaries are clean
        List<FCProduct> products = new ArrayList<>();
        Map<String, Integer> outboundCounts = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            FCProduct fp = createActiveFCProduct("SKU-" + String.format("%03d", i));
            products.add(fp);
            // Higher index = more outbound = higher velocity
            outboundCounts.put(fp.getId().value(), (i + 1) * 100);
        }

        List<FCProduct> changed = service.recalculateAll(products, outboundCounts);

        // Top 20% (indices 8, 9 when sorted by outbound desc) should be A
        // Products with highest outbound counts should get velocity A
        long velocityACount = products.stream()
                .filter(p -> p.getVelocity() == VelocityClass.A)
                .count();
        assertEquals(2, velocityACount); // 20% of 10
    }

    @Test
    void recalculateAll_assignsDToBottom20Percent() {
        List<FCProduct> products = new ArrayList<>();
        Map<String, Integer> outboundCounts = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            FCProduct fp = createActiveFCProduct("SKU-" + String.format("%03d", i));
            products.add(fp);
            outboundCounts.put(fp.getId().value(), (i + 1) * 100);
        }

        service.recalculateAll(products, outboundCounts);

        // Bottom 20% should be D
        long velocityDCount = products.stream()
                .filter(p -> p.getVelocity() == VelocityClass.D)
                .count();
        assertEquals(2, velocityDCount); // 20% of 10
    }

    @Test
    void recalculateAll_withEmptyList_returnsEmpty() {
        List<FCProduct> result = service.recalculateAll(Collections.emptyList(), Collections.emptyMap());

        assertTrue(result.isEmpty());
    }

    @Test
    void recalculateAll_returnsOnlyChangedProducts() {
        // All products start at velocity D. Create 10 products.
        List<FCProduct> products = new ArrayList<>();
        Map<String, Integer> outboundCounts = new HashMap<>();

        for (int i = 0; i < 10; i++) {
            FCProduct fp = createActiveFCProduct("SKU-" + String.format("%03d", i));
            products.add(fp);
            outboundCounts.put(fp.getId().value(), (i + 1) * 100);
        }

        List<FCProduct> changed = service.recalculateAll(products, outboundCounts);

        // All start as D. After recalculation, bottom 20% stay D, rest change.
        // So 8 out of 10 should be changed (indices 0-7 in desc sorted become A, B, C).
        // Bottom 2 stay D -> not changed. Top 8 change -> changed.
        assertEquals(8, changed.size());

        // Verify the unchanged ones are still D
        long unchangedD = products.stream()
                .filter(p -> p.getVelocity() == VelocityClass.D)
                .count();
        assertEquals(2, unchangedD);
    }
}
