package com.coupang.product.domain.model.converter;

import com.coupang.product.domain.model.fcproduct.FCProductId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FCProductIdConverter implements AttributeConverter<FCProductId, String> {

    @Override
    public String convertToDatabaseColumn(FCProductId id) {
        return id != null ? id.value() : null;
    }

    @Override
    public FCProductId convertToEntityAttribute(String s) {
        return s != null ? new FCProductId(s) : null;
    }
}
