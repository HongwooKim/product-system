package com.coupang.product.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AssignSlottingRequest(
        @NotBlank String primaryLocation,
        @NotBlank String replenishLocation,
        String bulkLocation,
        @Positive int pickFaceCapacity
) {}
