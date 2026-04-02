package com.coupang.product.domain.model.converter.type;

import com.coupang.product.domain.model.fcproduct.FCProductId;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class FCProductIdJavaType extends AbstractClassJavaType<FCProductId> {

    public static final FCProductIdJavaType INSTANCE = new FCProductIdJavaType();

    public FCProductIdJavaType() {
        super(FCProductId.class);
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return VarcharJdbcType.INSTANCE;
    }

    @Override
    public String toString(FCProductId value) {
        return value == null ? null : value.value();
    }

    @Override
    public FCProductId fromString(CharSequence string) {
        return string == null ? null : new FCProductId(string.toString());
    }

    @Override
    public <X> X unwrap(FCProductId value, Class<X> type, WrapperOptions options) {
        if (value == null) return null;
        if (String.class.isAssignableFrom(type)) {
            return type.cast(value.value());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> FCProductId wrap(X value, WrapperOptions options) {
        if (value == null) return null;
        if (value instanceof String s) {
            return new FCProductId(s);
        }
        throw unknownWrap(value.getClass());
    }
}
