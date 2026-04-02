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
 * 재슬로팅 배치.
 * 매주 월요일 03:00에 실행.
 *
 * Velocity 재산정 이후 "골든존에 있어야 하는데 없는" / "골든존에서 빠져야 하는데 남아있는"
 * 상품을 탐색하고 재슬로팅 이벤트를 발행한다.
 *
 * 흐름:
 * 1. 최근 velocity 변경이 있는 ACTIVE 상품 조회
 * 2. 현재 위치가 velocity 등급에 맞는지 검증
 * 3. 부적합 상품에 대해 재슬로팅 제안(ReslotCandidate) 생성
 *    → 실제 위치 이동은 재고 시스템과 피킹 시스템이 처리
 *    → 상품 시스템은 "이 상품을 옮겨야 한다"는 이벤트만 발행
 */
@Service
public class ReslottingBatchService {

    private static final Logger log = LoggerFactory.getLogger(ReslottingBatchService.class);
    private static final int VELOCITY_CHANGE_WINDOW_DAYS = 7;

    private final FCProductRepository fcProductRepository;
    private final DomainEventPublisher eventPublisher;

    public ReslottingBatchService(FCProductRepository fcProductRepository,
                                   DomainEventPublisher eventPublisher) {
        this.fcProductRepository = fcProductRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BatchResult execute(String warehouseId) {
        log.info("[배치-재슬로팅] 시작: FC={}", warehouseId);
        long startTime = System.currentTimeMillis();

        List<FCProduct> activeProducts = fcProductRepository
                .findByWarehouseIdAndStatus(new WarehouseId(warehouseId), FCProductStatus.ACTIVE);

        List<ReslotCandidate> candidates = new ArrayList<>();

        for (FCProduct product : activeProducts) {
            if (!product.hasRecentVelocityChange(VELOCITY_CHANGE_WINDOW_DAYS)) {
                continue;
            }
            if (product.getSlottingInfo() == null) {
                continue;
            }

            // Velocity A인데 골든존이 아닌 경우 → 골든존으로 이동 제안
            // Velocity D인데 골든존인 경우 → 외곽으로 이동 제안
            // (실제 골든존 여부 판단은 Location 데이터 필요. 여기서는 이벤트만 발행.)
            VelocityClass velocity = product.getVelocity();

            if (velocity.shouldBeInGoldenZone() || velocity.shouldBeInBulkZone()) {
                candidates.add(new ReslotCandidate(
                        product.getId().value(),
                        product.getSku().value(),
                        warehouseId,
                        velocity,
                        product.getSlottingInfo().primaryLocation().value(),
                        velocity.shouldBeInGoldenZone() ? "TO_GOLDEN_ZONE" : "TO_BULK_ZONE"
                ));
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[배치-재슬로팅] 완료: FC={}, 검토={}, 재슬로팅 대상={}, 소요={}ms",
                warehouseId, activeProducts.size(), candidates.size(), elapsed);

        return new BatchResult(warehouseId, "RESLOTTING",
                activeProducts.size(), candidates.size(), elapsed);
    }

    public record ReslotCandidate(
            String fcProductId,
            String sku,
            String warehouseId,
            VelocityClass velocity,
            String currentLocation,
            String action
    ) {}
}
