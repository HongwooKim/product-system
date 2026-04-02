package com.coupang.product.domain.model.fcproduct;

import java.util.Objects;
import java.util.Optional;

/**
 * 보관 프로파일. "이 상품은 이 FC에서 어떤 환경에 보관해야 하는가"를 정의.
 */
public record StorageProfile(
        TemperatureZone temperatureZone,
        StorageType storageType,
        int maxStackHeight,
        String hazardClass
) {
    public StorageProfile {
        Objects.requireNonNull(temperatureZone);
        Objects.requireNonNull(storageType);
        if (maxStackHeight < 1) {
            throw new IllegalArgumentException("maxStackHeight must be >= 1");
        }
    }

    public boolean requiresColdChain() {
        return temperatureZone.isColdChain();
    }

    public boolean isHazardous() {
        return hazardClass != null && !hazardClass.isBlank();
    }

    public Optional<String> getHazardClass() {
        return Optional.ofNullable(hazardClass);
    }
}
