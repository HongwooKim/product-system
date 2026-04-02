package com.coupang.product.domain.model.common;

import java.time.Instant;
import java.util.UUID;

/**
 * 모든 도메인 이벤트의 베이스.
 */
public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public abstract String getEventType();
}
