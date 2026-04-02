package com.coupang.product.domain.port;

import com.coupang.product.domain.model.fcproduct.StorageType;
import com.coupang.product.domain.model.fcproduct.TemperatureZone;
import com.coupang.product.domain.model.product.FulfillmentType;

import java.util.Set;

/**
 * FC 상품 커맨드 포트.
 * Consumer 등 외부 진입점이 이 인터페이스를 통해 FC 상품 명령을 실행한다.
 * 구현은 api 모듈의 FCProductCommandService.
 */
public interface FCProductCommandPort {

    String registerFCProduct(
            String sku, String warehouseId,
            TemperatureZone temperatureZone, StorageType storageType,
            int maxStackHeight, String hazardClass,
            boolean fifo, boolean expiryManaged, boolean lotTracking, boolean fragile,
            Set<FulfillmentType> fulfillmentTypes
    );

    void evaluateReplenishment(String fcProductId, int currentPickFaceQty);
}
