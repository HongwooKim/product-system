package com.coupang.product.application.batch;

import com.coupang.product.domain.port.DomainEventPublisher;
import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.repository.FCProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 유통기한 임박 상품 점검 배치.
 * 매일 04:00에 실행.
 *
 * expiryManaged=true인 FC 상품 중, 재고 시스템의 유통기한 데이터를 참조하여
 * 임박/만료 상품을 탐지하고 FC 상품 정지 또는 경고 이벤트를 발행한다.
 *
 * 흐름:
 * 1. expiryManaged=true인 ACTIVE 상품 조회
 * 2. 재고 시스템에 유통기한 조회 요청 (OutboundDataPort 확장 또는 별도 Port)
 * 3. D-2 이하 → FCProduct 자동 정지 (SuspensionReason.EXPIRY_RISK)
 * 4. D-7 이하 → 경고 로그 + 할인 판매 트리거 이벤트 발행
 *
 * ※ 현재는 재고 시스템 연동 전이므로, 대상 상품 목록만 추출하는 수준.
 *    실제 유통기한 데이터는 재고 시스템이 소유.
 */
@Service
public class ExpiryCheckBatchService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckBatchService.class);

    private final FCProductRepository fcProductRepository;
    private final DomainEventPublisher eventPublisher;

    public ExpiryCheckBatchService(FCProductRepository fcProductRepository,
                                    DomainEventPublisher eventPublisher) {
        this.fcProductRepository = fcProductRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BatchResult execute(String warehouseId) {
        log.info("[배치-유통기한] 시작: FC={}", warehouseId);
        long startTime = System.currentTimeMillis();

        List<FCProduct> activeProducts = fcProductRepository
                .findByWarehouseIdAndStatus(new WarehouseId(warehouseId), FCProductStatus.ACTIVE);

        // 유통기한 관리 대상 필터
        List<FCProduct> expiryManaged = activeProducts.stream()
                .filter(p -> p.getHandlingRule().expiryManaged())
                .toList();

        List<ExpiryAlert> alerts = new ArrayList<>();
        int suspendedCount = 0;

        for (FCProduct product : expiryManaged) {
            // TODO: 재고 시스템에서 해당 상품의 로트별 유통기한 조회
            // int daysToExpiry = inventoryPort.getMinDaysToExpiry(product.getId().value());
            //
            // if (daysToExpiry <= 2) {
            //     product.suspend(SuspensionReason.EXPIRY_RISK);
            //     fcProductRepository.save(product);
            //     eventPublisher.publishAll(product.getDomainEvents());
            //     product.clearDomainEvents();
            //     suspendedCount++;
            // } else if (daysToExpiry <= 7) {
            //     alerts.add(new ExpiryAlert(product.getId().value(), product.getSku().value(),
            //             warehouseId, daysToExpiry, "APPROACHING"));
            // }

            // 현재는 대상 목록만 로깅
            alerts.add(new ExpiryAlert(
                    product.getId().value(),
                    product.getSku().value(),
                    warehouseId,
                    -1,
                    "NEEDS_INVENTORY_CHECK"
            ));
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[배치-유통기한] 완료: FC={}, 대상={}, 알림={}, 정지={}, 소요={}ms",
                warehouseId, expiryManaged.size(), alerts.size(), suspendedCount, elapsed);

        return new BatchResult(warehouseId, "EXPIRY_CHECK",
                expiryManaged.size(), alerts.size(), elapsed);
    }

    public record ExpiryAlert(
            String fcProductId,
            String sku,
            String warehouseId,
            int daysToExpiry,
            String alertType
    ) {}
}
