package com.coupang.product.domain.model.converter;

import com.coupang.product.domain.model.fcproduct.WarehouseId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WarehouseIdConverter implements AttributeConverter<WarehouseId, String> {

    @Override
    public String convertToDatabaseColumn(WarehouseId id) {
        return id != null ? id.value() : null;
    }

    @Override
    public WarehouseId convertToEntityAttribute(String s) {
        return s != null ? new WarehouseId(s) : null;
    }
}
