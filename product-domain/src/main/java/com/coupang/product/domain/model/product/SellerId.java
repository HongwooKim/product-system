package com.coupang.product.domain.model.product;

import java.util.Objects;

public record SellerId(String value) {

    public SellerId {
        Objects.requireNonNull(value, "SellerId must not be null");
    }

    @Override
    public String toString() {
        return value;
    }
}
