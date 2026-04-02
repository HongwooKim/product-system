package com.coupang.product.domain.port;

import com.coupang.product.domain.model.fcproduct.FCProduct;

import java.util.Optional;

/**
 * FC 상품 조회 포트.
 * Consumer 등 외부 진입점이 이 인터페이스를 통해 FC 상품을 조회한다.
 * 구현은 api 모듈의 FCProductQueryService.
 */
public interface FCProductQueryPort {

    Optional<FCProduct> getFCProductBySkuAndWarehouse(String sku, String warehouseId);
}
