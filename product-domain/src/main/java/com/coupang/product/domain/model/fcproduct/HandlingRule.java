package com.coupang.product.domain.model.fcproduct;

/**
 * 취급 규칙. "이 상품을 어떻게 다뤄야 하는가".
 * 피킹/입고/보충 시스템이 이 규칙을 참조한다.
 */
public record HandlingRule(
        boolean fifo,
        boolean expiryManaged,
        boolean lotTracking,
        boolean fragile
) {
    /**
     * 로켓프레시 상품용 강제 규칙.
     */
    public static HandlingRule forRocketFresh() {
        return new HandlingRule(true, true, true, false);
    }

    /**
     * 일반 상품용 기본 규칙.
     */
    public static HandlingRule standard() {
        return new HandlingRule(false, false, false, false);
    }
}
