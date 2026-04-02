package com.coupang.product.domain.port;

import com.coupang.product.domain.model.common.DomainEvent;

import java.util.List;

/**
 * 도메인 이벤트를 외부(Kafka)로 발행하는 포트.
 * Infrastructure 레이어에서 구현.
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
