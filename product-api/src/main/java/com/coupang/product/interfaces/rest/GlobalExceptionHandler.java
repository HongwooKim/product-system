package com.coupang.product.interfaces.rest;

import com.coupang.product.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomainException(DomainException e) {
        log.warn("Domain exception: code={}, message={}", e.getCode(), e.getMessage());

        HttpStatus status = switch (e.getCode()) {
            case "PRODUCT_NOT_FOUND", "FC_PRODUCT_NOT_FOUND", "OFFER_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "DUPLICATE_SKU", "DUPLICATE_OFFER", "DUPLICATE_FC_PRODUCT" -> HttpStatus.CONFLICT;
            case "INVALID_STATUS_TRANSITION", "NOT_OPERATIONAL", "PRODUCT_DISCONTINUED" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.BAD_REQUEST;
        };

        return ResponseEntity.status(status).body(Map.of(
                "code", e.getCode(),
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", "VALIDATION_ERROR",
                "message", e.getMessage(),
                "timestamp", Instant.now().toString()
        ));
    }
}
