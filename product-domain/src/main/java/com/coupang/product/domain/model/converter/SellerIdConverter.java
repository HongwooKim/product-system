package com.coupang.product.domain.model.converter;

import com.coupang.product.domain.model.product.SellerId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SellerIdConverter implements AttributeConverter<SellerId, String> {

    @Override
    public String convertToDatabaseColumn(SellerId id) {
        return id != null ? id.value() : null;
    }

    @Override
    public SellerId convertToEntityAttribute(String s) {
        return s != null ? new SellerId(s) : null;
    }
}
