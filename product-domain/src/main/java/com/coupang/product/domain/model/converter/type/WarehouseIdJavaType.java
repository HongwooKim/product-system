package com.coupang.product.domain.model.converter.type;

import com.coupang.product.domain.model.fcproduct.WarehouseId;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;

public class WarehouseIdJavaType extends AbstractClassJavaType<WarehouseId> {

    public static final WarehouseIdJavaType INSTANCE = new WarehouseIdJavaType();

    public WarehouseIdJavaType() {
        super(WarehouseId.class);
    }

    @Override
    public JdbcType getRecommendedJdbcType(JdbcTypeIndicators indicators) {
        return VarcharJdbcType.INSTANCE;
    }

    @Override
    public String toString(WarehouseId value) {
        return value == null ? null : value.value();
    }

    @Override
    public WarehouseId fromString(CharSequence string) {
        return string == null ? null : new WarehouseId(string.toString());
    }

    @Override
    public <X> X unwrap(WarehouseId value, Class<X> type, WrapperOptions options) {
        if (value == null) return null;
        if (String.class.isAssignableFrom(type)) {
            return type.cast(value.value());
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> WarehouseId wrap(X value, WrapperOptions options) {
        if (value == null) return null;
        if (value instanceof String s) {
            return new WarehouseId(s);
        }
        throw unknownWrap(value.getClass());
    }
}
