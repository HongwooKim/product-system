package com.coupang.product.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PickingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PickingEventConsumer.class);

    @KafkaListener(topics = "${kafka.topics.picking-outbound-completed}", groupId = "product-system-picking")
    public void handleOutboundCompleted(@Payload String message, Acknowledgment ack) {
        // velocity 재산정은 일 배치에서 수행
        ack.acknowledge();
    }
}
