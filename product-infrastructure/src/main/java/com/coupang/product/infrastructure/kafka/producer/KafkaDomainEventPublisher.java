package com.coupang.product.infrastructure.kafka.producer;

import com.coupang.product.domain.port.DomainEventPublisher;
import com.coupang.product.domain.event.*;
import com.coupang.product.domain.model.common.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 도메인 이벤트를 Kafka로 발행하는 구현체.
 *
 * 토픽 라우팅:
 * - ProductRegisteredEvent      → product.registered         → 입고/발주/재고/주문
 * - ProductUpdatedEvent         → product.updated            → 입고/주문/FC상품관리
 * - ProductDiscontinuedEvent    → product.discontinued       → 발주/입고/재고/주문
 * - SellerOfferChangedEvent     → product.seller-offer-changed → 주문/발주
 * - FCProductRegisteredEvent    → fcproduct.registered       → 입고/재고
 * - FCProductSlottingAssignedEvent → fcproduct.slotting-assigned → 재고/피킹/입고
 * - FCProductVelocityReclassifiedEvent → fcproduct.velocity-reclassified → 피킹
 * - FCProductSuspendedEvent     → fcproduct.suspended        → 입고/피킹/출고/주문
 * - ReplenishmentNeededEvent    → fcproduct.replenishment-needed → 피킹/재고
 */
@Component
public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.product-registered}") private String productRegisteredTopic;
    @Value("${kafka.topics.product-updated}") private String productUpdatedTopic;
    @Value("${kafka.topics.product-discontinued}") private String productDiscontinuedTopic;
    @Value("${kafka.topics.product-seller-offer-changed}") private String sellerOfferChangedTopic;
    @Value("${kafka.topics.fcproduct-registered}") private String fcProductRegisteredTopic;
    @Value("${kafka.topics.fcproduct-slotting-assigned}") private String fcProductSlottingAssignedTopic;
    @Value("${kafka.topics.fcproduct-velocity-reclassified}") private String fcProductVelocityTopic;
    @Value("${kafka.topics.fcproduct-suspended}") private String fcProductSuspendedTopic;
    @Value("${kafka.topics.fcproduct-replenishment-needed}") private String replenishmentNeededTopic;

    public KafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        String topic = resolveTopic(event);
        String key = resolveKey(event);

        log.info("Publishing domain event: type={}, topic={}, key={}, eventId={}",
                event.getEventType(), topic, key, event.getEventId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event: type={}, topic={}, eventId={}",
                        event.getEventType(), topic, event.getEventId(), ex);
            } else {
                log.debug("Event published: type={}, topic={}, partition={}, offset={}",
                        event.getEventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event) {
            case ProductEvents.Registered e -> productRegisteredTopic;
            case ProductEvents.Updated e -> productUpdatedTopic;
            case ProductEvents.Discontinued e -> productDiscontinuedTopic;
            case ProductEvents.SellerOfferChanged e -> sellerOfferChangedTopic;
            case FCProductEvents.Registered e -> fcProductRegisteredTopic;
            case FCProductEvents.SlottingAssigned e -> fcProductSlottingAssignedTopic;
            case FCProductEvents.VelocityReclassified e -> fcProductVelocityTopic;
            case FCProductEvents.Suspended e -> fcProductSuspendedTopic;
            case FCProductEvents.Reslotted e -> fcProductSlottingAssignedTopic;
            case FCProductEvents.Discontinued e -> fcProductSuspendedTopic;
            case ReplenishmentNeededEvent e -> replenishmentNeededTopic;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
        };
    }

    private String resolveKey(DomainEvent event) {
        return switch (event) {
            case ProductEvents.Base e -> e.getSku().value();
            case FCProductEvents.Base e -> e.getSku().value();
            case ReplenishmentNeededEvent e -> e.getSpec().sku().value();
            default -> event.getEventId();
        };
    }
}
