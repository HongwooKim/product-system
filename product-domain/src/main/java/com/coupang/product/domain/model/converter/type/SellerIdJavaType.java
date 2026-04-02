package com.coupang.product.domain.model.converter.type;

import com.coupang.product.domain.model.product.SellerId;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class SellerIdJavaType extends AbstractClassJavaType<SellerId> {

    public static final SellerIdJavaType INSTANCE = new SellerIdJavaType();

    public SellerIdJavaType() {
        super(SellerId.class);
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return VarcharJdbcType.INSTANCE;
    }

    @Override
    public String toString(SellerId value) {
        return value == null ? null : value.value();
    }

    @Override
    public SellerId fromString(CharSequence string) {
        return string == null ? null : new SellerId(string.toString());
    }

    @Override
    public <X> X unwrap(SellerId value, Class<X> type, WrapperOptions options) {
        if (value == null) return null;
        if (String.class.isAssignableFrom(type)) {
            return type.cast(value.value());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> SellerId wrap(X value, WrapperOptions options) {
        if (value == null) return null;
        if (value instanceof String s) {
            return new SellerId(s);
        }
        throw unknownWrap(value.getClass());
    }
}
