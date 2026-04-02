package com.coupang.product.infrastructure.config;

import com.coupang.product.application.port.outbound.OutboundDataPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OutboundDataPort 구현체.
 * 실제로는 피킹 시스템의 API를 호출하거나,
 * Kafka로 수신한 출고 실적을 로컬 캐시/DB에서 조회한다.
 *
 * 여기서는 stub 구현.
 */
@Component
public class OutboundDataPortImpl implements OutboundDataPort {

    private static final Logger log = LoggerFactory.getLogger(OutboundDataPortImpl.class);

    @Override
    public Map<String, Integer> getOutboundCounts(String warehouseId, int days) {
        log.info("Fetching outbound counts for FC={}, days={}", warehouseId, days);
        // 실제 구현: 피킹 시스템 API 호출 또는 로컬 DB 조회
        // stub: 빈 맵 반환
        return new HashMap<>();
    }
}
