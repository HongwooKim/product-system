package com.coupang.product.domain.model.product;

import java.math.BigDecimal;
import java.util.Objects;

public record Price(BigDecimal amount, String currency) {

    public Price {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(currency);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0 이상이어야 합니다");
        }
    }

    public static Price krw(BigDecimal amount) {
        return new Price(amount, "KRW");
    }

    public boolean isHigherThan(Price other) {
        return this.amount.compareTo(other.amount) > 0;
    }
}
