package com.coupang.product.domain.model.product;

import java.util.Objects;

/**
 * Stock Keeping Unit. 상품의 고유 식별 코드.
 */
public record SKU(String value) {

    public SKU {
        Objects.requireNonNull(value, "SKU must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("SKU must not be blank");
    }

    @Override
    public String toString() {
        return value;
    }
}
