package com.coupang.product.domain.rule;

/**
 * 규칙 하나를 표현하는 인터페이스.
 * 조건(Condition)이 맞으면 결과(Result)를 반환한다.
 *
 * @param <C> 규칙 평가에 필요한 컨텍스트 타입
 * @param <R> 규칙 평가 결과 타입
 */
public interface Rule<C, R> {

    /** 규칙 식별자 */
    String getId();

    /** 규칙 이름 (사람이 읽을 수 있는) */
    String getName();

    /** 우선순위. 낮을수록 먼저 평가. */
    int getPriority();

    /** 이 규칙이 주어진 컨텍스트에 적용 가능한지 */
    boolean matches(C context);

    /** 규칙 적용 결과 */
    R evaluate(C context);
}
