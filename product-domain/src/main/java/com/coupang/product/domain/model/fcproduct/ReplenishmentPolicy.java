package com.coupang.product.domain.model.fcproduct;

import com.coupang.product.domain.exception.DomainException;

/**
 * 보충 정책. 피킹 지번의 재고가 트리거 이하로 떨어지면 보충 작업을 생성한다.
 */
public record ReplenishmentPolicy(
        int minQty,
        int maxQty,
        int triggerPoint,
        ReplenishUnit replenishUnit
) {
    public ReplenishmentPolicy {
        validate(minQty, maxQty, triggerPoint);
    }

    public static ReplenishmentPolicy of(int minQty, int maxQty, int triggerPoint, ReplenishUnit unit) {
        return new ReplenishmentPolicy(minQty, maxQty, triggerPoint, unit);
    }

    /**
     * velocity 등급에 따른 기본 정책 생성.
     */
    public static ReplenishmentPolicy defaultFor(VelocityClass velocity) {
        return switch (velocity) {
            case A -> new ReplenishmentPolicy(10, 50, 15, ReplenishUnit.CASE);
            case B -> new ReplenishmentPolicy(5, 30, 10, ReplenishUnit.CASE);
            case C -> new ReplenishmentPolicy(3, 20, 5, ReplenishUnit.CASE);
            case D -> new ReplenishmentPolicy(1, 10, 3, ReplenishUnit.EACH);
        };
    }

    public boolean isTriggered(int currentQty) {
        return currentQty <= triggerPoint;
    }

    public int calculateReplenishQty(int currentQty) {
        return Math.max(0, maxQty - currentQty);
    }

    private static void validate(int minQty, int maxQty, int triggerPoint) {
        if (minQty < 0) throw new DomainException("INVALID_POLICY", "minQty는 0 이상이어야 합니다");
        if (triggerPoint < minQty) throw new DomainException("INVALID_POLICY", "triggerPoint는 minQty 이상이어야 합니다");
        if (maxQty <= triggerPoint) throw new DomainException("INVALID_POLICY", "maxQty는 triggerPoint 초과여야 합니다");
    }

    public enum ReplenishUnit {
        EACH, CASE, PALLET
    }
}
