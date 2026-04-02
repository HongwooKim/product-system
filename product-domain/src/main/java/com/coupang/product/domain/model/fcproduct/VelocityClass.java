package com.coupang.product.domain.model.fcproduct;

/**
 * 출고빈도 등급.
 * 28일 롤링 출고 실적 기준으로 산정.
 */
public enum VelocityClass {

    /** 상위 20% — 골든존 배치 */
    A(true, false),

    /** 상위 20~50% — 접근 용이한 위치 */
    B(false, false),

    /** 상위 50~80% — 일반 위치 */
    C(false, false),

    /** 하위 20% — 상층부/벌크 */
    D(false, true);

    private final boolean goldenZone;
    private final boolean bulkZone;

    VelocityClass(boolean goldenZone, boolean bulkZone) {
        this.goldenZone = goldenZone;
        this.bulkZone = bulkZone;
    }

    public boolean shouldBeInGoldenZone() { return goldenZone; }
    public boolean shouldBeInBulkZone() { return bulkZone; }
}
