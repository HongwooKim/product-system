package com.coupang.product.domain.event;

import com.coupang.product.domain.model.common.DomainEvent;
import com.coupang.product.domain.model.fcproduct.FCProduct.ReplenishmentSpec;

/**
 * 보충 필요 이벤트.
 * 소비자: 피킹 시스템(보충 Task 생성), 재고 시스템(보충 재고 예약)
 */
public class ReplenishmentNeededEvent extends DomainEvent {

    private final ReplenishmentSpec spec;

    public ReplenishmentNeededEvent(ReplenishmentSpec spec) {
        super();
        this.spec = spec;
    }

    @Override
    public String getEventType() {
        return "fcproduct.replenishment-needed";
    }

    public ReplenishmentSpec getSpec() { return spec; }
}
