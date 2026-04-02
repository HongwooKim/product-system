package com.coupang.product.interfaces.dto;

import com.coupang.product.domain.model.product.FulfillmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Set;

public record RegisterProductRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,
        @NotBlank String categoryCode,
        @NotBlank String categoryDisplayName,
        String parentCategoryCode,
        @Positive int lengthMm,
        @Positive int widthMm,
        @Positive int heightMm,
        @Positive int weightG,
        @NotNull Set<FulfillmentType> fulfillmentTypes,
        String barcode,
        boolean temperatureSensitive,
        boolean hazardous,
        String sellerId,
        BigDecimal sellingPrice,
        BigDecimal supplyPrice,
        int leadTimeDays
) {}
