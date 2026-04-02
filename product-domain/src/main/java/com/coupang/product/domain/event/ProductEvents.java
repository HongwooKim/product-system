package com.coupang.product.domain.event;

import com.coupang.product.domain.model.common.DomainEvent;
import com.coupang.product.domain.model.product.*;

import java.util.Set;

/**
 * Product Aggregate의 도메인 이벤트.
 */
public final class ProductEvents {
    private ProductEvents() {}

    public static abstract class Base extends DomainEvent {
        private final ProductId productId;
        private final SKU sku;

        protected Base(ProductId productId, SKU sku) {
            super();
            this.productId = productId;
            this.sku = sku;
        }

        public ProductId getProductId() { return productId; }
        public SKU getSku() { return sku; }
    }

    /** 소비자: 입고/발주/재고/주문 */
    public static class Registered extends Base {
        private final String productName;
        private final Category category;
        private final ProductDimension dimensions;
        private final Set<FulfillmentType> fulfillmentTypes;
        private final ProductStatus status;
        private final boolean temperatureSensitive;
        private final boolean hazardous;

        public Registered(ProductId productId, SKU sku, String productName,
                          Category category, ProductDimension dimensions,
                          Set<FulfillmentType> fulfillmentTypes, ProductStatus status,
                          boolean temperatureSensitive, boolean hazardous) {
            super(productId, sku);
            this.productName = productName;
            this.category = category;
            this.dimensions = dimensions;
            this.fulfillmentTypes = fulfillmentTypes;
            this.status = status;
            this.temperatureSensitive = temperatureSensitive;
            this.hazardous = hazardous;
        }

        @Override public String getEventType() { return "product.registered"; }
        public String getProductName() { return productName; }
        public Category getCategory() { return category; }
        public ProductDimension getDimensions() { return dimensions; }
        public Set<FulfillmentType> getFulfillmentTypes() { return fulfillmentTypes; }
        public ProductStatus getStatus() { return status; }
        public boolean isTemperatureSensitive() { return temperatureSensitive; }
        public boolean isHazardous() { return hazardous; }
    }

    /** 소비자: 입고(규격 변경), 주문(상품명) */
    public static class Updated extends Base {
        private final String productName;
        private final Category category;
        private final ProductDimension dimensions;
        private final ProductStatus status;

        public Updated(ProductId productId, SKU sku, String productName,
                       Category category, ProductDimension dimensions, ProductStatus status) {
            super(productId, sku);
            this.productName = productName;
            this.category = category;
            this.dimensions = dimensions;
            this.status = status;
        }

        @Override public String getEventType() { return "product.updated"; }
        public String getProductName() { return productName; }
        public Category getCategory() { return category; }
        public ProductDimension getDimensions() { return dimensions; }
        public ProductStatus getStatus() { return status; }
    }

    /** 소비자: 발주(중지), 입고(거부), 재고(잔여처리), 주문(판매중지) */
    public static class Discontinued extends Base {
        public Discontinued(ProductId productId, SKU sku) {
            super(productId, sku);
        }
        @Override public String getEventType() { return "product.discontinued"; }
    }

    /** 소비자: 주문(가격/위너), 발주(공급가) */
    public static class SellerOfferChanged extends Base {
        private final SellerOffer currentWinner;
        private final ProductStatus productStatus;

        public SellerOfferChanged(ProductId productId, SKU sku,
                                   SellerOffer currentWinner, ProductStatus productStatus) {
            super(productId, sku);
            this.currentWinner = currentWinner;
            this.productStatus = productStatus;
        }

        @Override public String getEventType() { return "product.seller-offer-changed"; }
        public SellerOffer getCurrentWinner() { return currentWinner; }
        public ProductStatus getProductStatus() { return productStatus; }
    }
}
