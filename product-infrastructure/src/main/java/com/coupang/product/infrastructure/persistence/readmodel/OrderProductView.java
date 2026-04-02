package com.coupang.product.infrastructure.persistence.readmodel;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 주문 시스템용 Read Model.
 *
 * 주문 시스템이 필요한 정보만 비정규화하여 한 테이블에.
 * Product + SellerOffer(위너) + FCProduct(활성 FC 수) 를 조합.
 *
 * Write Model(Product, FCProduct Aggregate)과 독립적으로 존재.
 * 도메인 이벤트를 소비하여 비동기로 갱신.
 */
@Entity
@Table(name = "read_order_product_view", indexes = {
        @Index(name = "idx_read_order_sku", columnList = "sku", unique = true),
        @Index(name = "idx_read_order_status", columnList = "purchasable")
})
public class OrderProductView {

    @Id
    private String sku;

    private String productName;
    private String categoryCode;
    private String productStatus;

    // 아이템 위너 정보
    private String winnerSellerId;
    private BigDecimal winnerPrice;
    private String winnerPriceCurrency;

    // FC 가용성 요약
    private int activeFCCount;
    private String activeFCList;           // "FC-PYEONGTAEK,FC-GIMPO" 쉼표 구분
    private boolean hasColdChainFC;

    // 물리 규격 (포장/배송 참고)
    private int lengthMm;
    private int widthMm;
    private int heightMm;
    private int weightG;
    private boolean temperatureSensitive;
    private boolean oversized;

    // 판매 가능 여부 (최종 판단)
    private boolean purchasable;

    private LocalDateTime lastUpdated;

    // Getters & Setters
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    public String getProductStatus() { return productStatus; }
    public void setProductStatus(String productStatus) { this.productStatus = productStatus; }
    public String getWinnerSellerId() { return winnerSellerId; }
    public void setWinnerSellerId(String winnerSellerId) { this.winnerSellerId = winnerSellerId; }
    public BigDecimal getWinnerPrice() { return winnerPrice; }
    public void setWinnerPrice(BigDecimal winnerPrice) { this.winnerPrice = winnerPrice; }
    public String getWinnerPriceCurrency() { return winnerPriceCurrency; }
    public void setWinnerPriceCurrency(String winnerPriceCurrency) { this.winnerPriceCurrency = winnerPriceCurrency; }
    public int getActiveFCCount() { return activeFCCount; }
    public void setActiveFCCount(int activeFCCount) { this.activeFCCount = activeFCCount; }
    public String getActiveFCList() { return activeFCList; }
    public void setActiveFCList(String activeFCList) { this.activeFCList = activeFCList; }
    public boolean isHasColdChainFC() { return hasColdChainFC; }
    public void setHasColdChainFC(boolean hasColdChainFC) { this.hasColdChainFC = hasColdChainFC; }
    public int getLengthMm() { return lengthMm; }
    public void setLengthMm(int lengthMm) { this.lengthMm = lengthMm; }
    public int getWidthMm() { return widthMm; }
    public void setWidthMm(int widthMm) { this.widthMm = widthMm; }
    public int getHeightMm() { return heightMm; }
    public void setHeightMm(int heightMm) { this.heightMm = heightMm; }
    public int getWeightG() { return weightG; }
    public void setWeightG(int weightG) { this.weightG = weightG; }
    public boolean isTemperatureSensitive() { return temperatureSensitive; }
    public void setTemperatureSensitive(boolean temperatureSensitive) { this.temperatureSensitive = temperatureSensitive; }
    public boolean isOversized() { return oversized; }
    public void setOversized(boolean oversized) { this.oversized = oversized; }
    public boolean isPurchasable() { return purchasable; }
    public void setPurchasable(boolean purchasable) { this.purchasable = purchasable; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
