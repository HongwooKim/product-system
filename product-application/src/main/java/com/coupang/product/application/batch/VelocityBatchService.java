package com.coupang.product.application.batch;

import com.coupang.product.application.command.FCProductCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Velocity 재산정 배치.
 * FCProductCommandService.recalculateVelocity()에 위임.
 * 배치 고유 관심사(로깅, 타이밍, BatchResult 래핑)만 담당.
 */
@Service
public class VelocityBatchService {

    private static final Logger log = LoggerFactory.getLogger(VelocityBatchService.class);

    private final FCProductCommandService fcProductCommandService;

    public VelocityBatchService(FCProductCommandService fcProductCommandService) {
        this.fcProductCommandService = fcProductCommandService;
    }

    public BatchResult execute(String warehouseId) {
        log.info("[배치-Velocity] 시작: FC={}", warehouseId);
        long startTime = System.currentTimeMillis();

        int changedCount = fcProductCommandService.recalculateVelocity(warehouseId);

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("[배치-Velocity] 완료: FC={}, 변경={}, 소요={}ms",
                warehouseId, changedCount, elapsed);

        return new BatchResult(warehouseId, "VELOCITY", 0, changedCount, elapsed);
    }
}
