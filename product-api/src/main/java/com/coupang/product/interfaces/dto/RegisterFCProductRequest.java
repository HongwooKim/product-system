package com.coupang.product.interfaces.dto;

import com.coupang.product.domain.model.fcproduct.StorageType;
import com.coupang.product.domain.model.fcproduct.TemperatureZone;
import com.coupang.product.domain.model.product.FulfillmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record RegisterFCProductRequest(
        @NotBlank String sku,
        @NotBlank String warehouseId,
        @NotNull TemperatureZone temperatureZone,
        @NotNull StorageType storageType,
        @Positive int maxStackHeight,
        String hazardClass,
        boolean fifo,
        boolean expiryManaged,
        boolean lotTracking,
        boolean fragile,
        @NotNull Set<FulfillmentType> fulfillmentTypes
) {}
