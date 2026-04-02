package com.coupang.product.domain.model.fcproduct;

/**
 * 온도대. FC 내 보관 구역의 온도 분류.
 */
public enum TemperatureZone {

    FROZEN(-25, -18, "냉동"),
    CHILLED(2, 5, "냉장"),
    AMBIENT(15, 25, "정온"),
    ROOM_TEMP(null, null, "상온");

    private final Integer minCelsius;
    private final Integer maxCelsius;
    private final String displayName;

    TemperatureZone(Integer minCelsius, Integer maxCelsius, String displayName) {
        this.minCelsius = minCelsius;
        this.maxCelsius = maxCelsius;
        this.displayName = displayName;
    }

    public boolean isColdChain() {
        return this == FROZEN || this == CHILLED;
    }

    public boolean isCompatibleWith(TemperatureZone other) {
        if (this == FROZEN) return other == FROZEN;
        if (this == CHILLED) return other == CHILLED;
        return !other.isColdChain();
    }

    public String getDisplayName() { return displayName; }
    public Integer getMinCelsius() { return minCelsius; }
    public Integer getMaxCelsius() { return maxCelsius; }
}
