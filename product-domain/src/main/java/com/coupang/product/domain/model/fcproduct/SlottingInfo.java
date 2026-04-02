package com.coupang.product.domain.model.fcproduct;

import com.coupang.product.domain.model.product.ProductDimension;

import java.util.Objects;
import java.util.Optional;

/**
 * 슬로팅 정보. "이 상품이 이 FC에서 어디에 위치하는가".
 * 피킹 지번, 보충 지번, 벌크 지번을 포함.
 */
public record SlottingInfo(
        LocationCode primaryLocation,
        LocationCode replenishLocation,
        LocationCode bulkLocation,
        int pickFaceCapacity
) {
    public SlottingInfo {
        Objects.requireNonNull(primaryLocation, "피킹 지번은 필수입니다");
        Objects.requireNonNull(replenishLocation, "보충 지번은 필수입니다");
        if (pickFaceCapacity <= 0) {
            throw new IllegalArgumentException("피킹면 용량은 양수여야 합니다");
        }
    }

    public Optional<LocationCode> getBulkLocation() {
        return Optional.ofNullable(bulkLocation);
    }

    public boolean hasBulkLocation() {
        return bulkLocation != null;
    }
}
