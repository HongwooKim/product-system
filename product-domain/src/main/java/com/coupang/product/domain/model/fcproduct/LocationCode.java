package com.coupang.product.domain.model.fcproduct;

import java.util.Objects;

/**
 * FC 내 물리적 위치 코드.
 * 형식: {Zone}-{Aisle}-{Rack}-{Level}-{Bin}
 * 예: "A-12-03-2-B" = A존 12번 통로 3번 랙 2단 B번 빈
 */
public record LocationCode(String value) {

    public LocationCode {
        Objects.requireNonNull(value, "LocationCode must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("LocationCode must not be blank");
    }

    @Override
    public String toString() {
        return value;
    }
}
