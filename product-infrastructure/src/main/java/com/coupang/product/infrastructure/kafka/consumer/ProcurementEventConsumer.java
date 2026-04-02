package com.coupang.product.infrastructure.kafka.consumer;

import com.coupang.product.application.query.FCProductQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ProcurementEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProcurementEventConsumer.class);

    private final FCProductQueryService fcProductQueryService;
    private final ObjectMapper objectMapper;

    public ProcurementEventConsumer(FCProductQueryService fcProductQueryService, ObjectMapper objectMapper) {
        this.fcProductQueryService = fcProductQueryService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${kafka.topics.procurement-po-created}", groupId = "product-system-procurement")
    public void handlePurchaseOrderCreated(@Payload String message, Acknowledgment ack) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String sku = json.get("sku").asText();
            String warehouseId = json.get("warehouseId").asText();

            fcProductQueryService.getFCProductBySkuAndWarehouse(sku, warehouseId)
                    .ifPresentOrElse(
                            fp -> { if (!fp.getStatus().isOperational()) log.warn("[발주→상품] 비활성: {} @ {}", sku, warehouseId); },
                            () -> log.warn("[발주→상품] FC 미등록: {} @ {}", sku, warehouseId)
                    );
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[발주→상품] 발주 처리 실패: {}", message, e);
        }
    }
}
