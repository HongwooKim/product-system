package com.coupang.product.domain.model.fcproduct;

import java.util.Objects;
import java.util.UUID;

public record FCProductId(String value) {

    public FCProductId {
        Objects.requireNonNull(value, "FCProductId must not be null");
    }

    public static FCProductId generate() {
        return new FCProductId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
