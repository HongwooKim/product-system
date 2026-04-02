package com.coupang.product.interfaces.rest;

import com.coupang.product.application.query.readmodel.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CQRS Read Model 전용 조회 API.
 *
 * Write API (ProductController, FCProductController)와 완전 분리.
 * Read Model은 도메인 이벤트를 소비하여 비동기 갱신되므로,
 * Write 직후 Read에 반영되지 않을 수 있다 (Eventual Consistency).
 *
 * ReadModelQueryService(application 레이어 인터페이스)를 통해 조회.
 * → product-api가 JPA를 직접 참조하지 않음. 의존 방향 준수.
 */
@RestController
@RequestMapping("/api/v1/read")
public class ReadModelController {

    private final ReadModelQueryService readModelQueryService;

    public ReadModelController(ReadModelQueryService readModelQueryService) {
        this.readModelQueryService = readModelQueryService;
    }

    // ═══ 주문 시스템용 ═══

    /**
     * 주문 시스템: 판매 가능 여부 + 가격 + 활성 FC → 단일 쿼리.
     */
    @GetMapping("/order/{sku}")
    public ResponseEntity<OrderProductViewDto> getOrderView(@PathVariable String sku) {
        return readModelQueryService.getOrderView(sku)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ═══ 피킹 시스템용 ═══

    /**
     * 피킹 시스템: 지번 + velocity + 취급규칙 → 단일 쿼리.
     */
    @GetMapping("/picking/{fcProductId}")
    public ResponseEntity<PickingProductViewDto> getPickingView(@PathVariable String fcProductId) {
        return readModelQueryService.getPickingView(fcProductId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 피킹 시스템: FC 내 전체 operational 상품 목록.
     */
    @GetMapping("/picking")
    public ResponseEntity<List<PickingProductViewDto>> getPickingViewsByWarehouse(
            @RequestParam String warehouseId) {
        return ResponseEntity.ok(
                readModelQueryService.getOperationalPickingViews(warehouseId));
    }

    /**
     * 피킹 시스템: 특정 지번의 상품 조회.
     */
    @GetMapping("/picking/by-location")
    public ResponseEntity<List<PickingProductViewDto>> getPickingViewsByLocation(
            @RequestParam String location) {
        return ResponseEntity.ok(
                readModelQueryService.getPickingViewsByLocation(location));
    }

    // ═══ 대시보드/운영팀용 ═══

    /**
     * 대시보드: FC별 상품 현황.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<List<DashboardProductViewDto>> getDashboard(
            @RequestParam String warehouseId,
            @RequestParam(required = false, defaultValue = "ACTIVE") String status) {
        return ResponseEntity.ok(
                readModelQueryService.getDashboardByStatus(warehouseId, status));
    }

    /**
     * 대시보드: FC별 Velocity 분포.
     */
    @GetMapping("/dashboard/velocity-distribution")
    public ResponseEntity<List<Object[]>> getVelocityDistribution(
            @RequestParam String warehouseId) {
        return ResponseEntity.ok(
                readModelQueryService.getVelocityDistribution(warehouseId));
    }

    /**
     * 대시보드: FC별 온도대 분포.
     */
    @GetMapping("/dashboard/temperature-distribution")
    public ResponseEntity<List<Object[]>> getTemperatureDistribution(
            @RequestParam String warehouseId) {
        return ResponseEntity.ok(
                readModelQueryService.getTemperatureDistribution(warehouseId));
    }
}
