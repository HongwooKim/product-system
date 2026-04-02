package com.coupang.product.application.query.readmodel;

import java.math.BigDecimal;

public record OrderProductViewDto(
        String sku,
        String productName,
        String categoryCode,
        String productStatus,
        String winnerSellerId,
        BigDecimal winnerPrice,
        String winnerPriceCurrency,
        int activeFCCount,
        String activeFCList,
        boolean hasColdChainFC,
        int lengthMm, int widthMm, int heightMm, int weightG,
        boolean temperatureSensitive,
        boolean oversized,
        boolean purchasable,
        String lastUpdated
) {}
