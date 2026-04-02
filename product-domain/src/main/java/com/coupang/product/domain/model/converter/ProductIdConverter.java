package com.coupang.product.domain.model.converter;

import com.coupang.product.domain.model.product.ProductId;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ProductIdConverter implements AttributeConverter<ProductId, String> {

    @Override
    public String convertToDatabaseColumn(ProductId id) {
        return id != null ? id.value() : null;
    }

    @Override
    public ProductId convertToEntityAttribute(String s) {
        return s != null ? new ProductId(s) : null;
    }
}
