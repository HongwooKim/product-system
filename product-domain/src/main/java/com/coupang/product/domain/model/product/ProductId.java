package com.coupang.product.domain.model.product;

import java.util.Objects;
import java.util.UUID;

public record ProductId(String value) {

    public ProductId {
        Objects.requireNonNull(value, "ProductId must not be null");
    }

    public static ProductId generate() {
        return new ProductId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
