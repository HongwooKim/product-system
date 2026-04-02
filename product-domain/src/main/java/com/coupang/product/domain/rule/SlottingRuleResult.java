package com.coupang.product.domain.rule;

/**
 * 슬로팅 규칙 평가 결과.
 * "이 상품은 이런 조건의 위치에 놓아야 한다."
 */
public record SlottingRuleResult(
        /** 배치 존 유형: GOLDEN_ZONE, STANDARD, UPPER_LEVEL, BULK */
        String zoneType,

        /** 권장 층 (null이면 제약 없음) */
        Integer recommendedFloor,

        /** 권장 랙 위치: EYE_LEVEL, LOWER, UPPER (null이면 제약 없음) */
        String rackLevel,

        /** 최소 피킹면 용량 */
        int minPickFaceCapacity,

        /** 규칙 설명 (운영자가 읽을 수 있는) */
        String reason
) {}
