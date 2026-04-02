package com.coupang.product.interfaces.rest;

import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.ProductDimension;
import com.coupang.product.domain.rule.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rule Engine 조회/평가 API.
 *
 * 운영자가 규칙 평가 결과를 미리 확인하거나,
 * "이 상품을 이 조건으로 등록하면 어떤 규칙이 적용되는가?"를 시뮬레이션.
 */
@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final ProductRuleService ruleService;

    public RuleController(ProductRuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * 슬로팅 규칙 시뮬레이션.
     * "이 조건의 상품은 어떤 존에 배치되어야 하는가?"
     */
    @PostMapping("/simulate/slotting")
    public ResponseEntity<SlottingRuleResult> simulateSlotting(
            @RequestBody SlottingSimulationRequest request) {
        SlottingRuleResult result = ruleService.evaluateSlotting(
                request.velocity(),
                request.temperatureZone(),
                request.storageType(),
                new ProductDimension(request.lengthMm(), request.widthMm(),
                        request.heightMm(), request.weightG()),
                request.hazardous(),
                request.fragile(),
                request.expiryManaged()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * 보충 정책 규칙 시뮬레이션.
     * "이 조건의 상품은 어떤 보충 정책이 적용되어야 하는가?"
     */
    @PostMapping("/simulate/replenishment")
    public ResponseEntity<ReplenishRuleResult> simulateReplenishment(
            @RequestBody ReplenishSimulationRequest request) {
        ReplenishRuleResult result = ruleService.evaluateReplenishment(
                request.velocity(),
                request.dailyAverageOutbound(),
                request.currentPickFaceQty(),
                request.currentMaxQty(),
                request.expiryManaged(),
                request.isPromotionActive()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * 제약조건 검증 시뮬레이션.
     * "이 조건으로 FC에 등록하면 위반사항이 있는가?"
     */
    @PostMapping("/simulate/constraints")
    public ResponseEntity<List<ConstraintViolation>> simulateConstraints(
            @RequestBody ConstraintSimulationRequest request) {
        try {
            List<ConstraintViolation> violations = ruleService.validateConstraints(
                    request.fulfillmentTypes(),
                    request.temperatureZone(),
                    request.storageType(),
                    new ProductDimension(request.lengthMm(), request.widthMm(),
                            request.heightMm(), request.weightG()),
                    request.fifo(),
                    request.expiryManaged(),
                    request.hazardous()
            );
            return ResponseEntity.ok(violations);
        } catch (com.coupang.product.domain.exception.DomainException e) {
            // ERROR 레벨 위반이 있으면 DomainException이 발생.
            // 시뮬레이션에서는 예외 대신 위반 목록으로 반환.
            return ResponseEntity.unprocessableEntity().body(List.of(
                    new ConstraintViolation(e.getCode(), e.getMessage(),
                            ConstraintViolation.Severity.ERROR)
            ));
        }
    }

    // ── Request DTOs ──

    public record SlottingSimulationRequest(
            VelocityClass velocity, TemperatureZone temperatureZone, StorageType storageType,
            int lengthMm, int widthMm, int heightMm, int weightG,
            boolean hazardous, boolean fragile, boolean expiryManaged) {}

    public record ReplenishSimulationRequest(
            VelocityClass velocity, int dailyAverageOutbound,
            int currentPickFaceQty, int currentMaxQty,
            boolean expiryManaged, boolean isPromotionActive) {}

    public record ConstraintSimulationRequest(
            Set<FulfillmentType> fulfillmentTypes, TemperatureZone temperatureZone,
            StorageType storageType,
            int lengthMm, int widthMm, int heightMm, int weightG,
            boolean fifo, boolean expiryManaged, boolean hazardous) {}
}
