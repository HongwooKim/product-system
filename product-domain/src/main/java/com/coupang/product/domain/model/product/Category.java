package com.coupang.product.domain.model.product;

import java.util.Objects;

/**
 * 상품 카테고리. 계층 구조를 코드로 표현.
 * 예: "FOOD > FRESH > DAIRY" → code="FOOD-FRESH-DAIRY"
 */
public record Category(
        String code,
        String displayName,
        String parentCode
) {
    public Category {
        Objects.requireNonNull(code);
        Objects.requireNonNull(displayName);
    }

    public boolean isTopLevel() {
        return parentCode == null;
    }
}
