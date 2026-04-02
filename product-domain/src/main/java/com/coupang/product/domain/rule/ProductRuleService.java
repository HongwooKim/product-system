package com.coupang.product.domain.rule;

import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.ProductDimension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 상품 규칙 평가 도메인 서비스.
 *
 * 기존 DDD에서 Aggregate 안에 하드코딩되어 있던 규칙들을
 * Rule Engine으로 외부화한다.
 *
 * 변경 전: FCProduct.enforceHandlingRuleConstraints() 안에 if문
 * 변경 후: ProductRuleService.validateConstraints() → Rule Engine 평가
 *
 * 규칙은 RuleRepository에서 로딩. DB/설정 파일에서 읽어오므로
 * 코드 배포 없이 런타임에 규칙 변경 가능.
 */
public class ProductRuleService {

    private final RuleEngine<SlottingRuleContext, SlottingRuleResult> slottingEngine;
    private final RuleEngine<ReplenishRuleContext, ReplenishRuleResult> replenishEngine;
    private final RuleEngine<ConstraintRuleContext, ConstraintViolation> constraintEngine;

    public ProductRuleService(RuleRepository ruleRepository) {
        this.slottingEngine = new RuleEngine<>(ruleRepository.loadSlottingRules());
        this.replenishEngine = new RuleEngine<>(ruleRepository.loadReplenishRules());
        this.constraintEngine = new RuleEngine<>(ruleRepository.loadConstraintRules());
    }

    /**
     * 슬로팅 규칙 평가.
     * "이 상품은 어떤 존에, 어떤 높이에, 얼마나 넓은 피킹면에 놓아야 하는가?"
     *
     * 기존: VelocityClass.shouldBeInGoldenZone() 하드코딩
     * 변경: velocity + 온도대 + 규격 + 위험물 여부를 종합 평가
     */
    public SlottingRuleResult evaluateSlotting(
            VelocityClass velocity,
            TemperatureZone temperatureZone,
            StorageType storageType,
            ProductDimension dimensions,
            boolean hazardous,
            boolean fragile,
            boolean expiryManaged
    ) {
        SlottingRuleContext context = new SlottingRuleContext(
                velocity, temperatureZone, storageType, dimensions,
                hazardous, fragile, expiryManaged, 0
        );

        return slottingEngine.evaluateFirst(context)
                .orElse(new SlottingRuleResult(
                        "STANDARD", null, null, 10,
                        "기본 규칙: 일반 위치 배치"
                ));
    }

    /**
     * 보충 정책 규칙 평가.
     * "이 상품의 보충 트리거/최대수량/단위를 어떻게 설정할 것인가?"
     *
     * 기존: ReplenishmentPolicy.defaultFor(velocity) 하드코딩
     * 변경: velocity + 일평균출고 + 프로모션 여부 등을 종합 평가
     */
    public ReplenishRuleResult evaluateReplenishment(
            VelocityClass velocity,
            int dailyAverageOutbound,
            int currentPickFaceQty,
            int currentMaxQty,
            boolean expiryManaged,
            boolean isPromotionActive
    ) {
        ReplenishRuleContext context = new ReplenishRuleContext(
                velocity, dailyAverageOutbound, currentPickFaceQty,
                currentMaxQty, expiryManaged, isPromotionActive
        );

        return replenishEngine.evaluateFirst(context)
                .orElse(new ReplenishRuleResult(
                        1, 10, 3, "EACH",
                        "기본 규칙: 최소 보충 정책"
                ));
    }

    /**
     * 제약조건 검증.
     * "이 상품을 이 조건으로 FC에 등록할 수 있는가?"
     *
     * 기존: FCProduct.enforceHandlingRuleConstraints() 안에 if문
     * 변경: 모든 제약조건 규칙을 평가하고, ERROR 레벨 위반이 있으면 예외
     *
     * @return 위반 목록 (WARNING 포함). ERROR가 있으면 예외 발생.
     */
    public List<ConstraintViolation> validateConstraints(
            Set<FulfillmentType> fulfillmentTypes,
            TemperatureZone temperatureZone,
            StorageType storageType,
            ProductDimension dimensions,
            boolean fifo,
            boolean expiryManaged,
            boolean hazardous
    ) {
        ConstraintRuleContext context = new ConstraintRuleContext(
                fulfillmentTypes, temperatureZone, storageType,
                dimensions, fifo, expiryManaged, hazardous
        );

        List<ConstraintViolation> violations = constraintEngine.evaluateAll(context);

        // ERROR 레벨 위반이 있으면 등록 거부
        Optional<ConstraintViolation> error = violations.stream()
                .filter(v -> v.severity() == ConstraintViolation.Severity.ERROR)
                .findFirst();

        if (error.isPresent()) {
            throw new DomainException("CONSTRAINT_VIOLATION", error.get().message());
        }

        return violations;
    }
}
