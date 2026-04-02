package com.coupang.product.interfaces.rest;

import com.coupang.product.application.command.FCProductCommandService;
import com.coupang.product.application.query.FCProductQueryService;
import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.model.fcproduct.FCProductStatus;
import com.coupang.product.domain.model.fcproduct.SuspensionReason;
import com.coupang.product.interfaces.dto.AssignSlottingRequest;
import com.coupang.product.interfaces.dto.FCProductResponse;
import com.coupang.product.interfaces.dto.RegisterFCProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fc-products")
public class FCProductController {

    private final FCProductCommandService commandService;
    private final FCProductQueryService queryService;

    public FCProductController(FCProductCommandService commandService, FCProductQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * FC 상품 등록.
     * → Kafka: fcproduct.registered
     * → 소비자: 입고 시스템, 재고 시스템
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> registerFCProduct(
            @Valid @RequestBody RegisterFCProductRequest request) {
        String fcProductId = commandService.registerFCProduct(
                request.sku(), request.warehouseId(),
                request.temperatureZone(), request.storageType(),
                request.maxStackHeight(), request.hazardClass(),
                request.fifo(), request.expiryManaged(), request.lotTracking(), request.fragile(),
                request.fulfillmentTypes()
        );
        return ResponseEntity
                .created(URI.create("/api/v1/fc-products/" + fcProductId))
                .body(Map.of("fcProductId", fcProductId));
    }

    /**
     * 슬로팅 배정.
     * → Kafka: fcproduct.slotting-assigned
     * → 소비자: 재고(적치 위치 등록), 피킹(피킹 지번 등록), 입고(적치 안내)
     */
    @PostMapping("/{fcProductId}/slotting")
    public ResponseEntity<Void> assignSlotting(
            @PathVariable String fcProductId,
            @Valid @RequestBody AssignSlottingRequest request) {
        commandService.assignSlotting(
                fcProductId,
                request.primaryLocation(),
                request.replenishLocation(),
                request.bulkLocation(),
                request.pickFaceCapacity()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * FC 상품 상세 조회.
     */
    @GetMapping("/{fcProductId}")
    public ResponseEntity<FCProductResponse> getFCProduct(@PathVariable String fcProductId) {
        return queryService.getFCProduct(fcProductId)
                .map(FCProductResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * FC별 상품 목록 조회.
     */
    @GetMapping(params = {"warehouseId", "status"})
    public ResponseEntity<List<FCProductResponse>> getFCProductsByWarehouse(
            @RequestParam String warehouseId,
            @RequestParam FCProductStatus status) {
        List<FCProductResponse> products = queryService
                .getFCProductsByWarehouse(warehouseId, status).stream()
                .map(FCProductResponse::from).toList();
        return ResponseEntity.ok(products);
    }

    /**
     * SKU가 등록된 FC 목록 조회.
     * 주문 시스템에서 배송 가능 FC 확인용.
     */
    @GetMapping(params = "sku")
    public ResponseEntity<List<FCProductResponse>> getFCsByProduct(@RequestParam String sku) {
        List<FCProductResponse> products = queryService.getFCsByProduct(sku).stream()
                .map(FCProductResponse::from).toList();
        return ResponseEntity.ok(products);
    }

    /**
     * Velocity 전체 재산정 (배치 트리거).
     * → Kafka: fcproduct.velocity-reclassified (변경된 상품마다)
     */
    @PostMapping("/batch/velocity-recalculation")
    public ResponseEntity<Map<String, Object>> recalculateVelocity(
            @RequestParam String warehouseId) {
        int changed = commandService.recalculateVelocity(warehouseId);
        return ResponseEntity.ok(Map.of("warehouseId", warehouseId, "changedCount", changed));
    }

    /**
     * FC 상품 정지.
     * → Kafka: fcproduct.suspended
     */
    @PostMapping("/{fcProductId}/suspend")
    public ResponseEntity<Void> suspendFCProduct(
            @PathVariable String fcProductId,
            @RequestParam SuspensionReason reason) {
        commandService.suspendFCProduct(fcProductId, reason);
        return ResponseEntity.ok().build();
    }

    /**
     * FC 상품 재활성화.
     */
    @PostMapping("/{fcProductId}/reactivate")
    public ResponseEntity<Void> reactivateFCProduct(@PathVariable String fcProductId) {
        commandService.reactivateFCProduct(fcProductId);
        return ResponseEntity.ok().build();
    }
}
