package com.coupang.product.domain.rule;

import com.coupang.product.domain.model.fcproduct.VelocityClass;

/**
 * 보충 규칙 평가에 필요한 입력 데이터.
 * "이 상품의 보충 정책을 어떻게 설정할 것인가?"
 */
public record ReplenishRuleContext(
        VelocityClass velocity,
        int dailyAverageOutbound,
        int currentPickFaceQty,
        int currentMaxQty,
        boolean expiryManaged,
        boolean isPromotionActive
) {}
