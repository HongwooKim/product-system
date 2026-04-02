package com.coupang.product.config.batch;

import com.coupang.product.application.batch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 배치 스케줄러.
 * Spring @Scheduled로 각 배치 서비스를 정해진 시간에 트리거한다.
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │ 배치                    │ 주기      │ 시간   │ 설명         │
 * ├─────────────────────────┼───────────┼────────┼──────────────┤
 * │ Velocity 재산정         │ 매일      │ 02:00  │ 출고빈도 등급 │
 * │ 재슬로팅 탐색           │ 매주 월요일│ 03:00  │ 지번 재배정   │
 * │ 유통기한 점검           │ 매일      │ 04:00  │ 만료 임박 탐지│
 * │ 재고 건강도 점검        │ 매일      │ 05:00  │ 과잉/부족/체류│
 * └─────────────────────────────────────────────────────────────┘
 *
 * 각 배치는 전 FC를 순회하며 실행한다.
 */
@Component
public class BatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduler.class);

    private final VelocityBatchService velocityBatchService;
    private final ReslottingBatchService reslottingBatchService;
    private final ExpiryCheckBatchService expiryCheckBatchService;
    private final InventoryHealthBatchService inventoryHealthBatchService;

    @Value("${batch.warehouses:FC-PYEONGTAEK,FC-GIMPO,FC-DEOKPYEONG}")
    private List<String> warehouses;

    @Value("${batch.enabled:true}")
    private boolean batchEnabled;

    public BatchScheduler(VelocityBatchService velocityBatchService,
                           ReslottingBatchService reslottingBatchService,
                           ExpiryCheckBatchService expiryCheckBatchService,
                           InventoryHealthBatchService inventoryHealthBatchService) {
        this.velocityBatchService = velocityBatchService;
        this.reslottingBatchService = reslottingBatchService;
        this.expiryCheckBatchService = expiryCheckBatchService;
        this.inventoryHealthBatchService = inventoryHealthBatchService;
    }

    /**
     * Velocity 재산정 — 매일 02:00
     */
    @Scheduled(cron = "${batch.velocity.cron:0 0 2 * * *}")
    public void runVelocityBatch() {
        if (!batchEnabled) return;

        log.info("═══ [배치] Velocity 재산정 시작 ═══");
        for (String warehouseId : warehouses) {
            try {
                BatchResult result = velocityBatchService.execute(warehouseId);
                log.info("[배치] Velocity 결과: FC={}, 전체={}, 변경={}",
                        result.warehouseId(), result.totalProcessed(), result.affectedCount());
            } catch (Exception e) {
                log.error("[배치] Velocity 실패: FC={}", warehouseId, e);
            }
        }
        log.info("═══ [배치] Velocity 재산정 완료 ═══");
    }

    /**
     * 재슬로팅 탐색 — 매주 월요일 03:00
     */
    @Scheduled(cron = "${batch.reslotting.cron:0 0 3 * * MON}")
    public void runReslottingBatch() {
        if (!batchEnabled) return;

        log.info("═══ [배치] 재슬로팅 탐색 시작 ═══");
        for (String warehouseId : warehouses) {
            try {
                BatchResult result = reslottingBatchService.execute(warehouseId);
                log.info("[배치] 재슬로팅 결과: FC={}, 검토={}, 대상={}",
                        result.warehouseId(), result.totalProcessed(), result.affectedCount());
            } catch (Exception e) {
                log.error("[배치] 재슬로팅 실패: FC={}", warehouseId, e);
            }
        }
        log.info("═══ [배치] 재슬로팅 탐색 완료 ═══");
    }

    /**
     * 유통기한 점검 — 매일 04:00
     */
    @Scheduled(cron = "${batch.expiry.cron:0 0 4 * * *}")
    public void runExpiryCheckBatch() {
        if (!batchEnabled) return;

        log.info("═══ [배치] 유통기한 점검 시작 ═══");
        for (String warehouseId : warehouses) {
            try {
                BatchResult result = expiryCheckBatchService.execute(warehouseId);
                log.info("[배치] 유통기한 결과: FC={}, 대상={}, 알림={}",
                        result.warehouseId(), result.totalProcessed(), result.affectedCount());
            } catch (Exception e) {
                log.error("[배치] 유통기한 실패: FC={}", warehouseId, e);
            }
        }
        log.info("═══ [배치] 유통기한 점검 완료 ═══");
    }

    /**
     * 재고 건강도 점검 — 매일 05:00
     */
    @Scheduled(cron = "${batch.inventory-health.cron:0 0 5 * * *}")
    public void runInventoryHealthBatch() {
        if (!batchEnabled) return;

        log.info("═══ [배치] 재고 건강도 점검 시작 ═══");
        for (String warehouseId : warehouses) {
            try {
                BatchResult result = inventoryHealthBatchService.execute(warehouseId);
                log.info("[배치] 재고건강도 결과: FC={}, 검토={}, 알림={}",
                        result.warehouseId(), result.totalProcessed(), result.affectedCount());
            } catch (Exception e) {
                log.error("[배치] 재고건강도 실패: FC={}", warehouseId, e);
            }
        }
        log.info("═══ [배치] 재고 건강도 점검 완료 ═══");
    }
}
