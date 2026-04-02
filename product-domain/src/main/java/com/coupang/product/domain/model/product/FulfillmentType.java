package com.coupang.product.domain.model.product;

/**
 * 상품이 지원하는 풀필먼트 유형.
 * 물류 운영 전반의 규칙을 결정하는 핵심 분류.
 */
public enum FulfillmentType {

    /** 로켓배송 — 쿠팡 직매입, 자체 물류 */
    ROCKET,

    /** 로켓프레시 — 신선식품, 냉장/냉동 필수, FIFO 강제 */
    ROCKET_FRESH,

    /** 로켓직구 — 해외 직구, 통관 프로세스 포함 */
    ROCKET_GLOBAL,

    /** 마켓플레이스 — 셀러 직접 배송 */
    MARKETPLACE,

    /** 로켓그로스 — 셀러 상품 쿠팡 물류 위탁 */
    ROCKET_GROWTH
}
