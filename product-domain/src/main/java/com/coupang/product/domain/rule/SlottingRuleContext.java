package com.coupang.product.domain.rule;

import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.ProductDimension;

/**
 * 슬로팅 규칙 평가에 필요한 입력 데이터.
 * "이 상품을 어디에 놓을 것인가?"를 결정하기 위한 컨텍스트.
 */
public record SlottingRuleContext(
        VelocityClass velocity,
        TemperatureZone temperatureZone,
        StorageType storageType,
        ProductDimension dimensions,
        boolean hazardous,
        boolean fragile,
        boolean expiryManaged,
        int currentFloor
) {}
