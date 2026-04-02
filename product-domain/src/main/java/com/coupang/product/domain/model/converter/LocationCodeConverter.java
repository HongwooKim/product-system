package com.coupang.product.domain.model.converter;

import com.coupang.product.domain.model.fcproduct.LocationCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LocationCodeConverter implements AttributeConverter<LocationCode, String> {

    @Override
    public String convertToDatabaseColumn(LocationCode code) {
        return code != null ? code.value() : null;
    }

    @Override
    public LocationCode convertToEntityAttribute(String s) {
        return s != null ? new LocationCode(s) : null;
    }
}
