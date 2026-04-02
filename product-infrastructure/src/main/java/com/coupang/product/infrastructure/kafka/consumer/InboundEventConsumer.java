package com.coupang.product.infrastructure.kafka.consumer;

import com.coupang.product.application.command.FCProductCommandService;
import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.fcproduct.StorageType;
import com.coupang.product.domain.model.fcproduct.TemperatureZone;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class InboundEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(InboundEventConsumer.class);

    private final FCProductCommandService fcProductCommandService;
    private final ObjectMapper objectMapper;

    public InboundEventConsumer(FCProductCommandService fcProductCommandService, ObjectMapper objectMapper) {
        this.fcProductCommandService = fcProductCommandService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topics.inbound-asn-received}", groupId = "product-system-inbound")
    public void handleASNReceived(@Payload String message, Acknowledgment ack) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String sku = json.get("sku").asText();
            String warehouseId = json.get("warehouseId").asText();
            boolean expiryManaged = json.get("expiryManaged").asBoolean(false);

            fcProductCommandService.registerFCProduct(
                    sku, warehouseId,
                    TemperatureZone.valueOf(json.get("temperatureZone").asText()),
                    StorageType.valueOf(json.get("storageType").asText()),
                    3, null,
                    expiryManaged, expiryManaged, expiryManaged, false,
                    Set.of(FulfillmentType.ROCKET)
            );
            log.info("[입고→상품] FC 상품 등록: SKU={}, FC={}", sku, warehouseId);
            ack.acknowledge();
        } catch (DomainException e) {
            if ("DUPLICATE_FC_PRODUCT".equals(e.getCode())) {
                ack.acknowledge();
            } else {
                log.error("[입고→상품] ASN 처리 실패: {}", message, e);
            }
        } catch (Exception e) {
            log.error("[입고→상품] ASN 처리 실패: {}", message, e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.inbound-putaway-completed}", groupId = "product-system-inbound")
    public void handlePutawayCompleted(@Payload String message, Acknowledgment ack) {
        ack.acknowledge();
    }
}
