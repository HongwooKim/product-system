package com.coupang.product.domain.model.converter.type;

import com.coupang.product.domain.model.fcproduct.LocationCode;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class LocationCodeJavaType extends AbstractClassJavaType<LocationCode> {

    public static final LocationCodeJavaType INSTANCE = new LocationCodeJavaType();

    public LocationCodeJavaType() {
        super(LocationCode.class);
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return VarcharJdbcType.INSTANCE;
    }

    @Override
    public String toString(LocationCode value) {
        return value == null ? null : value.value();
    }

    @Override
    public LocationCode fromString(CharSequence string) {
        return string == null ? null : new LocationCode(string.toString());
    }

    @Override
    public <X> X unwrap(LocationCode value, Class<X> type, WrapperOptions options) {
        if (value == null) return null;
        if (String.class.isAssignableFrom(type)) {
            return type.cast(value.value());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> LocationCode wrap(X value, WrapperOptions options) {
        if (value == null) return null;
        if (value instanceof String s) {
            return new LocationCode(s);
        }
        throw unknownWrap(value.getClass());
    }
}
