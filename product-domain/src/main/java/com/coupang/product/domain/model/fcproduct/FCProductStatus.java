package com.coupang.product.domain.model.fcproduct;

public enum FCProductStatus {

    /** FC 등록 직후 — 보관 프로파일 확인, 규격 검증 중 */
    PENDING_SETUP,

    /** 슬로팅 진행 중 — 지번 배정 대기 */
    SLOTTING,

    /** 활성 — 입고/피킹/보충 가능 */
    ACTIVE,

    /** 일시 정지 — 품질 이슈, 리콜 등 */
    SUSPENDED,

    /** 해제 — 이 FC에서 더 이상 취급하지 않음 */
    DISCONTINUED;

    public boolean canTransitionTo(FCProductStatus target) {
        return switch (this) {
            case PENDING_SETUP -> target == SLOTTING || target == DISCONTINUED;
            case SLOTTING -> target == ACTIVE || target == DISCONTINUED;
            case ACTIVE -> target == SUSPENDED || target == DISCONTINUED;
            case SUSPENDED -> target == ACTIVE || target == DISCONTINUED;
            case DISCONTINUED -> false;
        };
    }

    public boolean isOperational() {
        return this == ACTIVE;
    }
}
