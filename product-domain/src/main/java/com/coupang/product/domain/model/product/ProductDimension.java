package com.coupang.product.domain.model.product;

/**
 * 상품 물리 규격. 물류 시스템 전체에서 참조하는 핵심 VO.
 * 적치, 피킹, 포장, 배송 모든 단계에서 사용.
 */
public record ProductDimension(
        int lengthMm,
        int widthMm,
        int heightMm,
        int weightG
) {
    public ProductDimension {
        if (lengthMm <= 0 || widthMm <= 0 || heightMm <= 0) {
            throw new IllegalArgumentException("치수는 양수여야 합니다");
        }
        if (weightG <= 0) {
            throw new IllegalArgumentException("무게는 양수여야 합니다");
        }
    }

    public long cubicVolumeCm3() {
        return (long) lengthMm * widthMm * heightMm / 1_000;
    }

    /**
     * MSC 셔틀 호환 규격: 600×400×400mm, 30kg 이하
     */
    public boolean fitsShuttleSpec() {
        return lengthMm <= 600 && widthMm <= 400
                && heightMm <= 400 && weightG <= 30_000;
    }

    /**
     * 소형 상품 여부. 경량 랙 배치 기준.
     */
    public boolean isSmall() {
        return cubicVolumeCm3() <= 15_000 && weightG <= 5_000;
    }

    /**
     * 대형 상품 여부. 평치 보관 기준.
     */
    public boolean isOversized() {
        return lengthMm > 1200 || widthMm > 800 || heightMm > 800 || weightG > 50_000;
    }
}
