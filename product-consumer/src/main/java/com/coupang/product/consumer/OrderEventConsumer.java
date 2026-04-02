package com.coupang.product.consumer;

import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.repository.FCProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 주문 시스템 이벤트 소비자.
 *
 * 주문 생성 시 상품의 FC별 운영 상태를 확인.
 * (주문 시스템은 상품 시스템의 product.registered, product.updated 등을 소비하여
 *  상품 정보를 자체 캐시하지만, 주문 생성 시 최신 상태를 재확인할 수 있다.)
 */
@Component
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final FCProductRepository fcProductRepository;
    private final ObjectMapper objectMapper;

    public OrderEventConsumer(FCProductRepository fcProductRepository, ObjectMapper objectMapper) {
        this.fcProductRepository = fcProductRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Topic: order.placed
     * 발행자: 주문 시스템
     *
     * 메시지 예:
     * {
     *   "orderId": "ORD-20260402-001",
     *   "items": [{"sku": "SKU-12345", "qty": 2}],
     *   "deliveryAddress": {"region": "GYEONGGI"}
     * }
     */
    @KafkaListener(
            topics = "${kafka.topics.order-placed}",
            groupId = "product-system-order"
    )
    public void handleOrderPlaced(@Payload String message, Acknowledgment ack) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String orderId = json.get("orderId").asText();

            JsonNode items = json.get("items");
            for (JsonNode item : items) {
                String sku = item.get("sku").asText();
                List<FCProduct> fcProducts = fcProductRepository.findBySku(sku);

                long activeFCs = fcProducts.stream()
                        .filter(fp -> fp.getStatus().isOperational())
                        .count();

                if (activeFCs == 0) {
                    log.warn("[주문→상품] 주문 {} 의 SKU {} 가 활성 FC가 없습니다", orderId, sku);
                }
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[주문→상품] 주문 처리 실패: {}", message, e);
        }
    }
}
