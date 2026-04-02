package com.coupang.product.domain.model.product;

import com.coupang.product.domain.model.converter.SellerIdConverter;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * 셀러별 판매 오퍼. 같은 상품을 여러 셀러가 판매할 수 있다.
 * Product Aggregate 내부 Entity.
 */
@Entity
@Table(name = "seller_offers")
public class SellerOffer {

    @Id
    @Column(length = 36)
    private String offerId;

    @Column(nullable = false, length = 36)
    @Convert(converter = SellerIdConverter.class)
    private SellerId sellerId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal sellingPriceAmount;

    @Column(nullable = false, length = 3)
    private String sellingPriceCurrency;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal supplyPriceAmount;

    @Column(nullable = false, length = 3)
    private String supplyPriceCurrency;

    private int leadTimeDays;
    private boolean winner;
    private boolean active;
    private LocalDateTime lastUpdated;

    public SellerOffer(SellerId sellerId, Price sellingPrice, Price supplyPrice, int leadTimeDays) {
        this.offerId = UUID.randomUUID().toString();
        this.sellerId = Objects.requireNonNull(sellerId);
        setSellingPrice(Objects.requireNonNull(sellingPrice));
        setSupplyPrice(Objects.requireNonNull(supplyPrice));
        this.leadTimeDays = leadTimeDays;
        this.winner = false;
        this.active = true;
        this.lastUpdated = LocalDateTime.now();
    }

    /** DB 복원용. 이벤트/검증 없이 전체 상태를 복원. */
    public static SellerOffer reconstitute(
            String offerId, SellerId sellerId, Price sellingPrice, Price supplyPrice,
            int leadTimeDays, boolean winner, boolean active, LocalDateTime lastUpdated
    ) {
        return new SellerOffer(offerId, sellerId, sellingPrice, supplyPrice,
                leadTimeDays, winner, active, lastUpdated);
    }

    private SellerOffer(String offerId, SellerId sellerId, Price sellingPrice, Price supplyPrice,
                        int leadTimeDays, boolean winner, boolean active, LocalDateTime lastUpdated) {
        this.offerId = offerId;
        this.sellerId = sellerId;
        setSellingPrice(sellingPrice);
        setSupplyPrice(supplyPrice);
        this.leadTimeDays = leadTimeDays;
        this.winner = winner;
        this.active = active;
        this.lastUpdated = lastUpdated;
    }

    protected SellerOffer() {}

    /**
     * 아이템 위너 점수 산정.
     * 가격, 리드타임, 셀러 신뢰도 등을 종합.
     * (여기서는 간략화 — 실제로는 ML 모델 등 활용)
     */
    public double calculateWinnerScore() {
        if (!active) return -1;
        // 낮은 가격 + 짧은 리드타임 = 높은 점수
        double priceScore = 1.0 / (1.0 + sellingPriceAmount.doubleValue());
        double leadTimeScore = 1.0 / (1.0 + leadTimeDays);
        return (priceScore * 0.7) + (leadTimeScore * 0.3);
    }

    public void updatePrice(Price newSellingPrice, Price newSupplyPrice) {
        setSellingPrice(Objects.requireNonNull(newSellingPrice));
        setSupplyPrice(Objects.requireNonNull(newSupplyPrice));
        this.lastUpdated = LocalDateTime.now();
    }

    public void markAsWinner() {
        this.winner = true;
    }

    public void revokeWinner() {
        this.winner = false;
    }

    public void deactivate() {
        this.active = false;
        this.winner = false;
    }

    public void activate() {
        this.active = true;
    }

    // ═══════════════════════════════════════
    //  Price VO ↔ flat columns helper
    // ═══════════════════════════════════════

    private void setSellingPrice(Price price) {
        this.sellingPriceAmount = price.amount();
        this.sellingPriceCurrency = price.currency();
    }

    private void setSupplyPrice(Price price) {
        this.supplyPriceAmount = price.amount();
        this.supplyPriceCurrency = price.currency();
    }

    // Getters
    public String getOfferId() { return offerId; }
    public SellerId getSellerId() { return sellerId; }
    public Price getSellingPrice() { return new Price(sellingPriceAmount, sellingPriceCurrency); }
    public Price getSupplyPrice() { return new Price(supplyPriceAmount, supplyPriceCurrency); }
    public int getLeadTimeDays() { return leadTimeDays; }
    public boolean isWinner() { return winner; }
    public boolean isActive() { return active; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
}
