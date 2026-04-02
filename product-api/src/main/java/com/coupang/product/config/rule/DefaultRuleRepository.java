package com.coupang.product.config.rule;

import com.coupang.product.domain.model.fcproduct.StorageType;
import com.coupang.product.domain.model.fcproduct.VelocityClass;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.rule.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 기본 규칙 저장소 구현체.
 *
 * 현재: Java 코드로 규칙 정의 (이전에 Aggregate 안에 있던 것을 여기로 추출)
 * 향후: DB 테이블(rule_definitions)에서 로딩하여 런타임 변경 가능하게 확장
 *
 * 규칙 추가/변경 시 이 파일만 수정하면 된다.
 * Aggregate 코드(FCProduct.java)를 건드리지 않는다.
 */
@Component
public class DefaultRuleRepository implements RuleRepository {

    private static final Logger log = LoggerFactory.getLogger(DefaultRuleRepository.class);

    // ═══════════════════════════════════════
    //  슬로팅 규칙
    // ═══════════════════════════════════════

    @Override
    public List<Rule<SlottingRuleContext, SlottingRuleResult>> loadSlottingRules() {
        List<Rule<SlottingRuleContext, SlottingRuleResult>> rules = new ArrayList<>();

        // Rule 1: Velocity A (고회전) → 골든존
        rules.add(new Rule<>() {
            public String getId() { return "SLOT-001"; }
            public String getName() { return "고회전 상품 골든존 배치"; }
            public int getPriority() { return 10; }

            public boolean matches(SlottingRuleContext ctx) {
                return ctx.velocity() == VelocityClass.A;
            }

            public SlottingRuleResult evaluate(SlottingRuleContext ctx) {
                return new SlottingRuleResult(
                        "GOLDEN_ZONE", null, "EYE_LEVEL", 30,
                        "Velocity A: 피킹 동선 최적 위치, 넓은 피킹면"
                );
            }
        });

        // Rule 2: Velocity B → 접근 용이한 위치
        rules.add(new Rule<>() {
            public String getId() { return "SLOT-002"; }
            public String getName() { return "중회전 상품 표준 배치"; }
            public int getPriority() { return 20; }

            public boolean matches(SlottingRuleContext ctx) {
                return ctx.velocity() == VelocityClass.B;
            }

            public SlottingRuleResult evaluate(SlottingRuleContext ctx) {
                return new SlottingRuleResult(
                        "STANDARD", null, "LOWER", 20,
                        "Velocity B: 접근 용이한 위치"
                );
            }
        });

        // Rule 3: Velocity D (저회전) → 상층부/벌크
        rules.add(new Rule<>() {
            public String getId() { return "SLOT-003"; }
            public String getName() { return "저회전 상품 상층부 배치"; }
            public int getPriority() { return 30; }

            public boolean matches(SlottingRuleContext ctx) {
                return ctx.velocity() == VelocityClass.D;
            }

            public SlottingRuleResult evaluate(SlottingRuleContext ctx) {
                return new SlottingRuleResult(
                        "BULK", null, "UPPER", 5,
                        "Velocity D: 상층부/벌크 존, 최소 피킹면"
                );
            }
        });

        // Rule 4: 대형 상품 → 평치 보관
        rules.add(new Rule<>() {
            public String getId() { return "SLOT-004"; }
            public String getName() { return "대형 상품 평치 보관"; }
            public int getPriority() { return 5; }  // 최우선

            public boolean matches(SlottingRuleContext ctx) {
                return ctx.dimensions().isOversized();
            }

            public SlottingRuleResult evaluate(SlottingRuleContext ctx) {
                return new SlottingRuleResult(
                        "FLAT_STORAGE", 1, null, 3,
                        "대형 상품: 평치 보관 전용 구역, 1층 배치"
                );
            }
        });

        // Rule 5: MSC 호환 규격품 → MSC 셀
        rules.add(new Rule<>() {
            public String getId() { return "SLOT-005"; }
            public String getName() { return "MSC 규격품 자동화 배치"; }
            public int getPriority() { return 8; }

            public boolean matches(SlottingRuleContext ctx) {
                return ctx.storageType() == StorageType.MSC
                        && ctx.dimensions().fitsShuttleSpec();
            }

            public SlottingRuleResult evaluate(SlottingRuleContext ctx) {
                return new SlottingRuleResult(
                        "MSC_CELL", 4, null, 15,
                        "MSC 규격 충족: 4층 자동화 셀 배치"
                );
            }
        });

        // Rule 6: 냉동 상품 → 3층 전용
        rules.add(new Rule<>() {
            public String getId() { return "SLOT-006"; }
            public String getName() { return "냉동 상품 3층 전용"; }
            public int getPriority() { return 3; }

            public boolean matches(SlottingRuleContext ctx) {
                return ctx.temperatureZone() == com.coupang.product.domain.model.fcproduct.TemperatureZone.FROZEN;
            }

            public SlottingRuleResult evaluate(SlottingRuleContext ctx) {
                return new SlottingRuleResult(
                        "FROZEN_ZONE", 3, null, 10,
                        "냉동 상품: 3층 냉동 전용 구역만 가능"
                );
            }
        });

        // Rule 7: 위험물 → 격리 구역
        rules.add(new Rule<>() {
            public String getId() { return "SLOT-007"; }
            public String getName() { return "위험물 격리 배치"; }
            public int getPriority() { return 1; }  // 최최우선

            public boolean matches(SlottingRuleContext ctx) {
                return ctx.hazardous();
            }

            public SlottingRuleResult evaluate(SlottingRuleContext ctx) {
                return new SlottingRuleResult(
                        "HAZMAT_ISOLATED", 1, null, 5,
                        "위험물: 1층 격리 구역 전용, 소량 피킹면"
                );
            }
        });

        log.info("슬로팅 규칙 {}개 로딩", rules.size());
        return rules;
    }

    // ═══════════════════════════════════════
    //  보충 규칙
    // ═══════════════════════════════════════

    @Override
    public List<Rule<ReplenishRuleContext, ReplenishRuleResult>> loadReplenishRules() {
        List<Rule<ReplenishRuleContext, ReplenishRuleResult>> rules = new ArrayList<>();

        // Rule 1: 프로모션 활성 → 보충량 2배
        rules.add(new Rule<>() {
            public String getId() { return "REPL-001"; }
            public String getName() { return "프로모션 활성 시 보충량 증가"; }
            public int getPriority() { return 5; }

            public boolean matches(ReplenishRuleContext ctx) {
                return ctx.isPromotionActive();
            }

            public ReplenishRuleResult evaluate(ReplenishRuleContext ctx) {
                int boostedMax = Math.max(ctx.currentMaxQty() * 2, ctx.dailyAverageOutbound() * 3);
                return new ReplenishRuleResult(
                        ctx.currentMaxQty() / 2, boostedMax, boostedMax / 3, "CASE",
                        "프로모션 활성: 보충량 2배, 트리거 포인트 상향"
                );
            }
        });

        // Rule 2: Velocity A + 일평균출고 높음 → 공격적 보충
        rules.add(new Rule<>() {
            public String getId() { return "REPL-002"; }
            public String getName() { return "고회전 상품 공격적 보충"; }
            public int getPriority() { return 10; }

            public boolean matches(ReplenishRuleContext ctx) {
                return ctx.velocity() == VelocityClass.A && ctx.dailyAverageOutbound() > 10;
            }

            public ReplenishRuleResult evaluate(ReplenishRuleContext ctx) {
                int max = ctx.dailyAverageOutbound() * 3;
                return new ReplenishRuleResult(
                        max / 5, max, max / 3, "CASE",
                        "Velocity A + 고출고: 3일분 보관, 1일분 이하 시 보충"
                );
            }
        });

        // Rule 3: Velocity A 기본
        rules.add(new Rule<>() {
            public String getId() { return "REPL-003"; }
            public String getName() { return "Velocity A 기본 보충"; }
            public int getPriority() { return 15; }

            public boolean matches(ReplenishRuleContext ctx) {
                return ctx.velocity() == VelocityClass.A;
            }

            public ReplenishRuleResult evaluate(ReplenishRuleContext ctx) {
                return new ReplenishRuleResult(10, 50, 15, "CASE",
                        "Velocity A: 표준 보충 정책");
            }
        });

        // Rule 4: Velocity B
        rules.add(new Rule<>() {
            public String getId() { return "REPL-004"; }
            public String getName() { return "Velocity B 보충"; }
            public int getPriority() { return 20; }

            public boolean matches(ReplenishRuleContext ctx) {
                return ctx.velocity() == VelocityClass.B;
            }

            public ReplenishRuleResult evaluate(ReplenishRuleContext ctx) {
                return new ReplenishRuleResult(5, 30, 10, "CASE",
                        "Velocity B: 중간 보충 정책");
            }
        });

        // Rule 5: Velocity C
        rules.add(new Rule<>() {
            public String getId() { return "REPL-005"; }
            public String getName() { return "Velocity C 보충"; }
            public int getPriority() { return 30; }

            public boolean matches(ReplenishRuleContext ctx) {
                return ctx.velocity() == VelocityClass.C;
            }

            public ReplenishRuleResult evaluate(ReplenishRuleContext ctx) {
                return new ReplenishRuleResult(3, 20, 5, "CASE",
                        "Velocity C: 소량 보충 정책");
            }
        });

        // Rule 6: Velocity D (기본 최소)
        rules.add(new Rule<>() {
            public String getId() { return "REPL-006"; }
            public String getName() { return "Velocity D 최소 보충"; }
            public int getPriority() { return 40; }

            public boolean matches(ReplenishRuleContext ctx) {
                return ctx.velocity() == VelocityClass.D;
            }

            public ReplenishRuleResult evaluate(ReplenishRuleContext ctx) {
                return new ReplenishRuleResult(1, 10, 3, "EACH",
                        "Velocity D: 최소 보충, 개별 단위");
            }
        });

        // Rule 7: 유통기한 관리 상품 → 소량 빈번 보충
        rules.add(new Rule<>() {
            public String getId() { return "REPL-007"; }
            public String getName() { return "유통기한 관리 상품 소량 보충"; }
            public int getPriority() { return 3; }  // 높은 우선순위

            public boolean matches(ReplenishRuleContext ctx) {
                return ctx.expiryManaged();
            }

            public ReplenishRuleResult evaluate(ReplenishRuleContext ctx) {
                // 유통기한 상품은 피킹면에 많이 쌓으면 안 됨 (만료 위험)
                int max = Math.min(ctx.dailyAverageOutbound() * 2, 20);
                max = Math.max(max, 5);
                return new ReplenishRuleResult(
                        1, max, max / 3, "CASE",
                        "유통기한 관리: 소량 빈번 보충, 2일분 이하 보관"
                );
            }
        });

        log.info("보충 규칙 {}개 로딩", rules.size());
        return rules;
    }

    // ═══════════════════════════════════════
    //  제약조건 규칙
    // ═══════════════════════════════════════

    @Override
    public List<Rule<ConstraintRuleContext, ConstraintViolation>> loadConstraintRules() {
        List<Rule<ConstraintRuleContext, ConstraintViolation>> rules = new ArrayList<>();

        // Rule 1: 로켓프레시 → FIFO + 유통기한 관리 필수
        rules.add(new Rule<>() {
            public String getId() { return "CONST-001"; }
            public String getName() { return "로켓프레시 FIFO 필수"; }
            public int getPriority() { return 10; }

            public boolean matches(ConstraintRuleContext ctx) {
                return ctx.fulfillmentTypes().contains(FulfillmentType.ROCKET_FRESH)
                        && (!ctx.fifo() || !ctx.expiryManaged());
            }

            public ConstraintViolation evaluate(ConstraintRuleContext ctx) {
                return new ConstraintViolation(
                        "ROCKET_FRESH_FIFO",
                        "로켓프레시 상품은 FIFO와 유통기한 관리가 필수입니다",
                        ConstraintViolation.Severity.ERROR
                );
            }
        });

        // Rule 2: 로켓프레시 → 냉장/냉동 필수
        rules.add(new Rule<>() {
            public String getId() { return "CONST-002"; }
            public String getName() { return "로켓프레시 콜드체인 필수"; }
            public int getPriority() { return 10; }

            public boolean matches(ConstraintRuleContext ctx) {
                return ctx.fulfillmentTypes().contains(FulfillmentType.ROCKET_FRESH)
                        && !ctx.temperatureZone().isColdChain();
            }

            public ConstraintViolation evaluate(ConstraintRuleContext ctx) {
                return new ConstraintViolation(
                        "ROCKET_FRESH_TEMP",
                        "로켓프레시 상품은 냉장 또는 냉동 온도대만 가능합니다",
                        ConstraintViolation.Severity.ERROR
                );
            }
        });

        // Rule 3: MSC 보관 → 셔틀 규격 필수
        rules.add(new Rule<>() {
            public String getId() { return "CONST-003"; }
            public String getName() { return "MSC 셔틀 규격 검증"; }
            public int getPriority() { return 20; }

            public boolean matches(ConstraintRuleContext ctx) {
                return ctx.storageType() == StorageType.MSC
                        && !ctx.dimensions().fitsShuttleSpec();
            }

            public ConstraintViolation evaluate(ConstraintRuleContext ctx) {
                return new ConstraintViolation(
                        "MSC_DIMENSION",
                        "MSC 보관 상품은 600×400×400mm, 30kg 이하여야 합니다 (현재: "
                                + ctx.dimensions().lengthMm() + "×"
                                + ctx.dimensions().widthMm() + "×"
                                + ctx.dimensions().heightMm() + "mm, "
                                + ctx.dimensions().weightG() + "g)",
                        ConstraintViolation.Severity.ERROR
                );
            }
        });

        // Rule 4: 대형 상품 + 경량 랙 → 경고
        rules.add(new Rule<>() {
            public String getId() { return "CONST-004"; }
            public String getName() { return "대형 상품 경량랙 경고"; }
            public int getPriority() { return 30; }

            public boolean matches(ConstraintRuleContext ctx) {
                return ctx.dimensions().isOversized()
                        && ctx.storageType() == StorageType.SHELF_RACK;
            }

            public ConstraintViolation evaluate(ConstraintRuleContext ctx) {
                return new ConstraintViolation(
                        "OVERSIZED_SHELF",
                        "대형 상품을 경량 랙에 배치하면 효율이 떨어집니다. 평치 보관을 권장합니다.",
                        ConstraintViolation.Severity.WARNING
                );
            }
        });

        // Rule 5: 위험물 + 식품 혼재 금지
        rules.add(new Rule<>() {
            public String getId() { return "CONST-005"; }
            public String getName() { return "위험물-식품 혼재 금지"; }
            public int getPriority() { return 5; }

            public boolean matches(ConstraintRuleContext ctx) {
                return ctx.hazardous()
                        && ctx.fulfillmentTypes().contains(FulfillmentType.ROCKET_FRESH);
            }

            public ConstraintViolation evaluate(ConstraintRuleContext ctx) {
                return new ConstraintViolation(
                        "HAZMAT_FOOD",
                        "위험물은 식품(로켓프레시)과 같은 FC에 등록할 수 없습니다",
                        ConstraintViolation.Severity.ERROR
                );
            }
        });

        log.info("제약조건 규칙 {}개 로딩", rules.size());
        return rules;
    }
}
