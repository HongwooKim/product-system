package com.coupang.product.domain.rule;

/**
 * 제약조건 위반 결과.
 * 위반이 없으면 규칙이 매칭되지 않는다.
 */
public record ConstraintViolation(
        /** 위반 코드 */
        String code,

        /** 위반 설명 */
        String message,

        /** 심각도: ERROR(등록 거부), WARNING(경고만) */
        Severity severity
) {
    public enum Severity { ERROR, WARNING }
}
