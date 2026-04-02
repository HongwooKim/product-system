package com.coupang.product.interfaces.dto;

import com.coupang.product.domain.model.product.*;

import java.util.List;
import java.util.Set;

public record ProductResponse(
        String productId,
        String sku,
        String name,
        String categoryCode,
        String categoryDisplayName,
        DimensionDto dimensions,
        String status,
        Set<FulfillmentType> fulfillmentTypes,
        List<SellerOfferDto> sellerOffers,
        SellerOfferDto currentWinner,
        boolean temperatureSensitive,
        boolean hazardous,
        String createdAt,
        String updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId().value(),
                product.getSku().value(),
                product.getName(),
                product.getCategory().code(),
                product.getCategory().displayName(),
                new DimensionDto(
                        product.getDimensions().lengthMm(),
                        product.getDimensions().widthMm(),
                        product.getDimensions().heightMm(),
                        product.getDimensions().weightG(),
                        product.getDimensions().cubicVolumeCm3()
                ),
                product.getStatus().name(),
                product.getFulfillmentTypes(),
                product.getSellerOffers().stream().map(SellerOfferDto::from).toList(),
                product.getCurrentWinner().map(SellerOfferDto::from).orElse(null),
                product.isTemperatureSensitive(),
                product.isHazardous(),
                product.getCreatedAt().toString(),
                product.getUpdatedAt().toString()
        );
    }

    public record DimensionDto(int lengthMm, int widthMm, int heightMm, int weightG, long cubicVolumeCm3) {}

    public record SellerOfferDto(String sellerId, String sellingPrice, String supplyPrice,
                                  int leadTimeDays, boolean winner, boolean active) {
        public static SellerOfferDto from(SellerOffer offer) {
            return new SellerOfferDto(
                    offer.getSellerId().value(),
                    offer.getSellingPrice().amount().toString(),
                    offer.getSupplyPrice().amount().toString(),
                    offer.getLeadTimeDays(),
                    offer.isWinner(),
                    offer.isActive()
            );
        }
    }
}
