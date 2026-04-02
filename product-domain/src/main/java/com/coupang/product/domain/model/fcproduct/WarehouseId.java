package com.coupang.product.domain.model.fcproduct;

import java.util.Objects;

/**
 * 물류센터(FC) 식별자.
 * 예: "FC-PYEONGTAEK", "FC-GIMPO", "FC-DEOKPYEONG"
 */
public record WarehouseId(String value) {

    public WarehouseId {
        Objects.requireNonNull(value, "WarehouseId must not be null");
    }

    @Override
    public String toString() {
        return value;
    }
}
