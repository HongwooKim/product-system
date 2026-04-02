package com.coupang.product.domain.exception;

public class DomainException extends RuntimeException {

    private final String code;

    public DomainException(String code, String message) {
        super(message);
        this.code = code;
    }

    public DomainException(String message) {
        this("DOMAIN_ERROR", message);
    }

    public String getCode() {
        return code;
    }
}
