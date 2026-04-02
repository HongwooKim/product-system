package com.coupang.product.domain.rule;

import com.coupang.product.domain.model.fcproduct.StorageType;
import com.coupang.product.domain.model.fcproduct.TemperatureZone;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.ProductDimension;

import java.util.Set;

/**
 * 제약조건 규칙 평가에 필요한 입력 데이터.
 * "이 상품을 이 FC에 등록할 수 있는가?", "이 조합이 유효한가?"를 판단.
 */
public record ConstraintRuleContext(
        Set<FulfillmentType> fulfillmentTypes,
        TemperatureZone temperatureZone,
        StorageType storageType,
        ProductDimension dimensions,
        boolean fifo,
        boolean expiryManaged,
        boolean hazardous
) {}
