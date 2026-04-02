package com.coupang.product.domain.rule;

/**
 * 보충 규칙 평가 결과.
 * "이 상품의 보충 정책은 이렇게 설정해야 한다."
 */
public record ReplenishRuleResult(
        int minQty,
        int maxQty,
        int triggerPoint,
        String replenishUnit,
        String reason
) {}
