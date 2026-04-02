package com.coupang.product.application.port.outbound;

import java.util.Map;

/**
 * 외부 시스템(피킹 시스템) 데이터 조회 포트.
 * 출고 실적 등을 조회한다.
 */
public interface OutboundDataPort {

    /**
     * 특정 FC의 최근 N일간 FCProduct별 출고 횟수 조회.
     * 피킹 시스템에서 제공하는 데이터.
     *
     * @return Map<fcProductId, outboundCount>
     */
    Map<String, Integer> getOutboundCounts(String warehouseId, int days);
}
