package com.coupang.product.consumer;

import com.coupang.product.application.command.FCProductCommandService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final FCProductCommandService fcProductCommandService;
    private final ObjectMapper objectMapper;

    public InventoryEventConsumer(FCProductCommandService fcProductCommandService, ObjectMapper objectMapper) {
        this.fcProductCommandService = fcProductCommandService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topics.inventory-stock-deducted}", groupId = "product-system-inventory")
    public void handleStockDeducted(@Payload String message, Acknowledgment ack) {
        try {
            JsonNode json = objectMapper.readTree(message);
            fcProductCommandService.evaluateReplenishment(
                    json.get("fcProductId").asText(),
                    json.get("currentQty").asInt()
            );
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[재고→상품] 재고 차감 처리 실패: {}", message, e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.inventory-stock-level-changed}", groupId = "product-system-inventory")
    public void handleStockLevelChanged(@Payload String message, Acknowledgment ack) {
        ack.acknowledge();
    }
}
