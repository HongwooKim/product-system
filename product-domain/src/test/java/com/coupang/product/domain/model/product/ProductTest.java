package com.coupang.product.domain.model.product;

import com.coupang.product.domain.event.ProductEvents;
import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.common.DomainEvent;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    // ═══════════════════════════════════════
    //  Helper methods
    // ═══════════════════════════════════════

    private static SKU sku(String value) {
        return new SKU(value);
    }

    private static Category category() {
        return new Category("ELECTRONICS", "Electronics", null);
    }

    private static ProductDimension dimensions() {
        return new ProductDimension(300, 200, 100, 500);
    }

    private static Set<FulfillmentType> rocketFulfillment() {
        return Set.of(FulfillmentType.ROCKET);
    }

    private static Product registerStandardProduct() {
        return Product.register(
                sku("SKU-001"), "Test Product", "A test product",
                category(), dimensions(), rocketFulfillment(),
                "8801234567890", false, false
        );
    }

    private static SellerOffer createOffer(String sellerId, int sellingPrice, int supplyPrice, int leadTimeDays) {
        return new SellerOffer(
                new SellerId(sellerId),
                Price.krw(BigDecimal.valueOf(sellingPrice)),
                Price.krw(BigDecimal.valueOf(supplyPrice)),
                leadTimeDays
        );
    }

    private static Product reconstitutedProduct(ProductStatus status) {
        return Product.reconstitute(
                ProductId.generate(), sku("SKU-RECON"), "Reconstituted", "desc",
                category(), dimensions(), status,
                rocketFulfillment(), new ArrayList<>(),
                "barcode", "http://img.url",
                false, false,
                LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );
    }

    // ═══════════════════════════════════════
    //  register()
    // ═══════════════════════════════════════

    @Test
    void register_createsProductWithPendingReviewStatus() {
        Product product = registerStandardProduct();

        assertEquals(ProductStatus.PENDING_REVIEW, product.getStatus());
        assertEquals("SKU-001", product.getSku().value());
        assertEquals("Test Product", product.getName());
        assertNotNull(product.getId());
        assertNotNull(product.getCreatedAt());
    }

    @Test
    void register_raisesRegisteredEvent() {
        Product product = registerStandardProduct();

        List<DomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(ProductEvents.Registered.class, events.get(0));

        ProductEvents.Registered event = (ProductEvents.Registered) events.get(0);
        assertEquals(product.getId(), event.getProductId());
        assertEquals(product.getSku(), event.getSku());
        assertEquals(ProductStatus.PENDING_REVIEW, event.getStatus());
    }

    // ═══════════════════════════════════════
    //  approve()
    // ═══════════════════════════════════════

    @Test
    void approve_transitionsPendingReviewToActive() {
        Product product = registerStandardProduct();

        product.approve();

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
    }

    // ═══════════════════════════════════════
    //  discontinue()
    // ═══════════════════════════════════════

    @Test
    void discontinue_transitionsToDiscontinuedAndDeactivatesAllOffers() {
        Product product = registerStandardProduct();
        product.approve();
        product.addSellerOffer(createOffer("seller-1", 10000, 7000, 2));
        product.addSellerOffer(createOffer("seller-2", 12000, 8000, 3));

        product.discontinue();

        assertEquals(ProductStatus.DISCONTINUED, product.getStatus());
        assertTrue(product.getSellerOffers().stream().noneMatch(SellerOffer::isActive));
    }

    @Test
    void discontinue_onDiscontinuedProduct_throwsException() {
        Product product = reconstitutedProduct(ProductStatus.ACTIVE);
        product.discontinue();

        DomainException ex = assertThrows(DomainException.class, product::discontinue);
        assertEquals("INVALID_STATUS_TRANSITION", ex.getCode());
    }

    // ═══════════════════════════════════════
    //  addSellerOffer()
    // ═══════════════════════════════════════

    @Test
    void addSellerOffer_addsOfferAndRecalculatesWinner() {
        Product product = registerStandardProduct();
        product.clearDomainEvents();

        SellerOffer offer = createOffer("seller-1", 10000, 7000, 2);
        product.addSellerOffer(offer);

        assertEquals(1, product.getSellerOffers().size());
        assertTrue(product.getCurrentWinner().isPresent());
        assertEquals("seller-1", product.getCurrentWinner().get().getSellerId().value());
    }

    @Test
    void addSellerOffer_duplicateSeller_throwsException() {
        Product product = registerStandardProduct();
        product.addSellerOffer(createOffer("seller-1", 10000, 7000, 2));

        DomainException ex = assertThrows(DomainException.class,
                () -> product.addSellerOffer(createOffer("seller-1", 11000, 8000, 3)));
        assertEquals("DUPLICATE_OFFER", ex.getCode());
    }

    // ═══════════════════════════════════════
    //  updateDimensions()
    // ═══════════════════════════════════════

    @Test
    void updateDimensions_raisesUpdatedEvent() {
        Product product = registerStandardProduct();
        product.clearDomainEvents();

        ProductDimension newDimensions = new ProductDimension(400, 300, 200, 1000);
        product.updateDimensions(newDimensions);

        List<DomainEvent> events = product.getDomainEvents();
        assertEquals(1, events.size());
        assertInstanceOf(ProductEvents.Updated.class, events.get(0));

        ProductEvents.Updated event = (ProductEvents.Updated) events.get(0);
        assertEquals(newDimensions, event.getDimensions());
    }

    // ═══════════════════════════════════════
    //  ROCKET_FRESH constraint
    // ═══════════════════════════════════════

    @Test
    void register_rocketFreshWithoutTemperatureSensitive_throwsException() {
        DomainException ex = assertThrows(DomainException.class, () ->
                Product.register(
                        sku("SKU-FRESH"), "Fresh Product", "desc",
                        category(), dimensions(),
                        Set.of(FulfillmentType.ROCKET_FRESH),
                        "barcode", false, false
                ));
        assertEquals("INVALID_FULFILLMENT", ex.getCode());
    }

    // ═══════════════════════════════════════
    //  reconstitute()
    // ═══════════════════════════════════════

    @Test
    void reconstitute_doesNotRaiseEvents() {
        Product product = reconstitutedProduct(ProductStatus.ACTIVE);

        assertTrue(product.getDomainEvents().isEmpty());
    }
}
