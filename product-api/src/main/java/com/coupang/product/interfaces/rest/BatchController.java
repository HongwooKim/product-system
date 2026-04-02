package com.coupang.product.interfaces.rest;

import com.coupang.product.application.batch.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 배치 수동 실행 API.
 * 스케줄러가 아닌 운영자가 수동으로 배치를 트리거할 때 사용.
 */
@RestController
@RequestMapping("/api/v1/batch")
public class BatchController {

    private final VelocityBatchService velocityBatchService;
    private final ReslottingBatchService reslottingBatchService;
    private final ExpiryCheckBatchService expiryCheckBatchService;
    private final InventoryHealthBatchService inventoryHealthBatchService;

    public BatchController(VelocityBatchService velocityBatchService,
                            ReslottingBatchService reslottingBatchService,
                            ExpiryCheckBatchService expiryCheckBatchService,
                            InventoryHealthBatchService inventoryHealthBatchService) {
        this.velocityBatchService = velocityBatchService;
        this.reslottingBatchService = reslottingBatchService;
        this.expiryCheckBatchService = expiryCheckBatchService;
        this.inventoryHealthBatchService = inventoryHealthBatchService;
    }

    @PostMapping("/velocity")
    public ResponseEntity<BatchResult> runVelocity(@RequestParam String warehouseId) {
        return ResponseEntity.ok(velocityBatchService.execute(warehouseId));
    }

    @PostMapping("/reslotting")
    public ResponseEntity<BatchResult> runReslotting(@RequestParam String warehouseId) {
        return ResponseEntity.ok(reslottingBatchService.execute(warehouseId));
    }

    @PostMapping("/expiry-check")
    public ResponseEntity<BatchResult> runExpiryCheck(@RequestParam String warehouseId) {
        return ResponseEntity.ok(expiryCheckBatchService.execute(warehouseId));
    }

    @PostMapping("/inventory-health")
    public ResponseEntity<BatchResult> runInventoryHealth(@RequestParam String warehouseId) {
        return ResponseEntity.ok(inventoryHealthBatchService.execute(warehouseId));
    }
}
