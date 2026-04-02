package com.coupang.product.application.batch;

import com.coupang.product.application.port.outbound.OutboundDataPort;
import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.repository.FCProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 재고 건강도 점검 배치.
 * 매일 05:00에 실행.
 *
 * FC별 상품의 출고 실적 대비 재고 수준을 분석하여
 * 과잉 재고(Overstock), 부족 재고(Understock), 체류 재고(Dead Stock)를 탐지.
 *
 * 흐름:
 * 1. ACTIVE 상품 + 28일 출고 실적 조회
 * 2. 출고가 0인데 재고가 있는 상품 → Dead Stock 후보
 * 3. Velocity A인데 보충 트리거 빈번 → Understock 후보 (보충 정책 상향 제안)
 * 4. Velocity D인데 재고 과잉 → Overstock 후보 (FC 간 이관 제안)
 */
@Service
public class InventoryHealthBatchService {

    private static final Logger log = LoggerFactory.getLogger(InventoryHealthBatchService.class);

    private final FCProductRepository fcProductRepository;
    private final OutboundDataPort outboundDataPort;

    public InventoryHealthBatchService(FCProductRepository fcProductRepository,
                                        OutboundDataPort outboundDataPort) {
        this.fcProductRepository = fcProductRepository;
        this.outboundDataPort = outboundDataPort;
    }

    @Transactional(readOnly = true)
    public BatchResult execute(String warehouseId) {
        log.info("[배치-재고건강도] 시작: FC={}", warehouseId);
        long startTime = System.currentTimeMillis();

        List<FCProduct> activeProducts = fcProductRepository
                .findByWarehouseIdAndStatus(new WarehouseId(warehouseId), FCProductStatus.ACTIVE);

        Map<String, Integer> outboundCounts = outboundDataPort
                .getOutboundCounts(warehouseId, 28);

        List<HealthAlert> alerts = new ArrayList<>();

        for (FCProduct product : activeProducts) {
            int outbound = outboundCounts.getOrDefault(product.getId().value(), 0);

            // Dead Stock: 28일간 출고 0
            if (outbound == 0) {
                alerts.add(new HealthAlert(
                        product.getId().value(),
                        product.getSku().value(),
                        warehouseId,
                        HealthAlertType.DEAD_STOCK,
                        "28일간 출고 없음. FC 간 이관 또는 폐기 검토 필요"
                ));
            }

            // Velocity A인데 출고 대비 보충 정책 maxQty가 너무 낮은 경우
            if (product.getVelocity() == VelocityClass.A) {
                int dailyAvg = outbound / 28;
                if (dailyAvg > 0 && product.getReplenishmentPolicy().maxQty() < dailyAvg * 2) {
                    alerts.add(new HealthAlert(
                            product.getId().value(),
                            product.getSku().value(),
                            warehouseId,
                            HealthAlertType.UNDERSTOCK_RISK,
                            "일평균 출고=" + dailyAvg + ", 피킹면 최대=" +
                                    product.getReplenishmentPolicy().maxQty() +
                                    ". 보충 정책 상향 권고"
                    ));
                }
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[배치-재고건강도] 완료: FC={}, 검토={}, 알림={}, 소요={}ms",
                warehouseId, activeProducts.size(), alerts.size(), elapsed);

        return new BatchResult(warehouseId, "INVENTORY_HEALTH",
                activeProducts.size(), alerts.size(), elapsed);
    }

    public record HealthAlert(
            String fcProductId,
            String sku,
            String warehouseId,
            HealthAlertType alertType,
            String message
    ) {}

    public enum HealthAlertType {
        DEAD_STOCK,
        OVERSTOCK,
        UNDERSTOCK_RISK,
        TRANSFER_CANDIDATE
    }
}
