@JavaTypeRegistration(javaType = FCProductId.class, descriptorClass = FCProductIdJavaType.class)
@JavaTypeRegistration(javaType = ProductId.class, descriptorClass = ProductIdJavaType.class)
@JavaTypeRegistration(javaType = SKU.class, descriptorClass = SKUJavaType.class)
@JavaTypeRegistration(javaType = WarehouseId.class, descriptorClass = WarehouseIdJavaType.class)
@JavaTypeRegistration(javaType = SellerId.class, descriptorClass = SellerIdJavaType.class)
@JavaTypeRegistration(javaType = LocationCode.class, descriptorClass = LocationCodeJavaType.class)
package com.coupang.product.domain.model.converter;

import com.coupang.product.domain.model.converter.type.*;
import com.coupang.product.domain.model.fcproduct.FCProductId;
import com.coupang.product.domain.model.fcproduct.LocationCode;
import com.coupang.product.domain.model.fcproduct.WarehouseId;
import com.coupang.product.domain.model.product.ProductId;
import com.coupang.product.domain.model.product.SKU;
import com.coupang.product.domain.model.product.SellerId;
import org.hibernate.annotations.JavaTypeRegistration;
