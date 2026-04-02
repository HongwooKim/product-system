package com.coupang.product.domain.rule;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 범용 규칙 엔진.
 * 규칙 목록을 받아 우선순위 순으로 평가하고, 매칭되는 결과를 반환한다.
 *
 * 순수 Java. 프레임워크 의존 없음.
 * 규칙 자체는 외부(DB, 설정 파일)에서 로딩하여 주입.
 * → 규칙 변경 시 코드 배포 없이 런타임 반영 가능.
 */
public class RuleEngine<C, R> {

    private final List<Rule<C, R>> rules;

    public RuleEngine(List<Rule<C, R>> rules) {
        this.rules = rules.stream()
                .sorted(Comparator.comparingInt(Rule::getPriority))
                .collect(Collectors.toList());
    }

    /**
     * 첫 번째 매칭 규칙의 결과를 반환.
     * 우선순위가 높은(숫자가 낮은) 규칙부터 평가.
     */
    public Optional<R> evaluateFirst(C context) {
        for (Rule<C, R> rule : rules) {
            if (rule.matches(context)) {
                return Optional.of(rule.evaluate(context));
            }
        }
        return Optional.empty();
    }

    /**
     * 매칭되는 모든 규칙의 결과를 반환.
     */
    public List<R> evaluateAll(C context) {
        List<R> results = new ArrayList<>();
        for (Rule<C, R> rule : rules) {
            if (rule.matches(context)) {
                results.add(rule.evaluate(context));
            }
        }
        return results;
    }

    /**
     * 매칭되는 규칙이 하나라도 있는지 확인.
     * 제약조건 검증용 — "위반하는 규칙이 있는가?"
     */
    public boolean anyMatches(C context) {
        return rules.stream().anyMatch(r -> r.matches(context));
    }

    /**
     * 매칭되는 모든 규칙의 결과를 RuleResult로 감싸서 반환.
     * 어떤 규칙이 왜 매칭되었는지 추적 가능.
     */
    public List<RuleResult<R>> evaluateWithTrace(C context) {
        List<RuleResult<R>> results = new ArrayList<>();
        for (Rule<C, R> rule : rules) {
            if (rule.matches(context)) {
                results.add(new RuleResult<>(rule.getId(), rule.getName(), rule.evaluate(context)));
            }
        }
        return results;
    }

    public int ruleCount() {
        return rules.size();
    }

    public record RuleResult<R>(String ruleId, String ruleName, R result) {}
}
