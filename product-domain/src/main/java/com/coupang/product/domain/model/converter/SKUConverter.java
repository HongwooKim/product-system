package com.coupang.product.domain.model.converter;

import com.coupang.product.domain.model.product.SKU;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SKUConverter implements AttributeConverter<SKU, String> {

    @Override
    public String convertToDatabaseColumn(SKU sku) {
        return sku != null ? sku.value() : null;
    }

    @Override
    public SKU convertToEntityAttribute(String s) {
        return s != null ? new SKU(s) : null;
    }
}
