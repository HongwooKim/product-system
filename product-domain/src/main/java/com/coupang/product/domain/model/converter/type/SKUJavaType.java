package com.coupang.product.domain.model.converter.type;

import com.coupang.product.domain.model.product.SKU;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class SKUJavaType extends AbstractClassJavaType<SKU> {

    public static final SKUJavaType INSTANCE = new SKUJavaType();

    public SKUJavaType() {
        super(SKU.class);
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return VarcharJdbcType.INSTANCE;
    }

    @Override
    public String toString(SKU value) {
        return value == null ? null : value.value();
    }

    @Override
    public SKU fromString(CharSequence string) {
        return string == null ? null : new SKU(string.toString());
    }

    @Override
    public <X> X unwrap(SKU value, Class<X> type, WrapperOptions options) {
        if (value == null) return null;
        if (String.class.isAssignableFrom(type)) {
            return type.cast(value.value());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> SKU wrap(X value, WrapperOptions options) {
        if (value == null) return null;
        if (value instanceof String s) {
            return new SKU(s);
        }
        throw unknownWrap(value.getClass());
    }
}
