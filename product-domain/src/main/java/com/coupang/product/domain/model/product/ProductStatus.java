package com.coupang.product.domain.model.product;

public enum ProductStatus {

    /** 등록 대기 — 셀러가 등록 후 검수 전 */
    PENDING_REVIEW,

    /** 활성 — 판매 가능 */
    ACTIVE,

    /** 일시 정지 — 품질 이슈, 가격 이슈 등 */
    SUSPENDED,

    /** 단종 — 더 이상 판매/입고 불가 */
    DISCONTINUED;

    public boolean canTransitionTo(ProductStatus target) {
        return switch (this) {
            case PENDING_REVIEW -> target == ACTIVE || target == SUSPENDED;
            case ACTIVE -> target == SUSPENDED || target == DISCONTINUED;
            case SUSPENDED -> target == ACTIVE || target == DISCONTINUED;
            case DISCONTINUED -> false;
        };
    }
}
