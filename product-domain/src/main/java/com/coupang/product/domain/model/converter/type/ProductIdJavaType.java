package com.coupang.product.domain.model.converter.type;

import com.coupang.product.domain.model.product.ProductId;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class ProductIdJavaType extends AbstractClassJavaType<ProductId> {

    public static final ProductIdJavaType INSTANCE = new ProductIdJavaType();

    public ProductIdJavaType() {
        super(ProductId.class);
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return VarcharJdbcType.INSTANCE;
    }

    @Override
    public String toString(ProductId value) {
        return value == null ? null : value.value();
    }

    @Override
    public ProductId fromString(CharSequence string) {
        return string == null ? null : new ProductId(string.toString());
    }

    @Override
    public <X> X unwrap(ProductId value, Class<X> type, WrapperOptions options) {
        if (value == null) return null;
        if (String.class.isAssignableFrom(type)) {
            return type.cast(value.value());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> ProductId wrap(X value, WrapperOptions options) {
        if (value == null) return null;
        if (value instanceof String s) {
            return new ProductId(s);
        }
        throw unknownWrap(value.getClass());
    }
}
