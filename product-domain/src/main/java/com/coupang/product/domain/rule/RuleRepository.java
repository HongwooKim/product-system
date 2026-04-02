package com.coupang.product.domain.rule;

import java.util.List;

/**
 * 규칙 저장소 인터페이스.
 * 규칙을 외부(DB, 설정 파일)에서 로딩한다.
 * Infrastructure 레이어에서 구현.
 */
public interface RuleRepository {

    List<Rule<SlottingRuleContext, SlottingRuleResult>> loadSlottingRules();

    List<Rule<ReplenishRuleContext, ReplenishRuleResult>> loadReplenishRules();

    List<Rule<ConstraintRuleContext, ConstraintViolation>> loadConstraintRules();
}
