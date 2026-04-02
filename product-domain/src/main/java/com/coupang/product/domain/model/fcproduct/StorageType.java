package com.coupang.product.domain.model.fcproduct;

public enum StorageType {

    /** 팔레트 랙 — 대형, 중량 상품 */
    PALLET_RACK,

    /** 경량 랙 — 소형, 경량 상품 */
    SHELF_RACK,

    /** 평치 보관 — 비정형, 대형 상품 */
    FLAT_STORAGE,

    /** 멀티셔틀크레인 — 자동화, 규격품 */
    MSC
}
