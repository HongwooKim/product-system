package com.coupang.product.application.query;

import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.model.fcproduct.FCProductStatus;
import com.coupang.product.domain.model.fcproduct.VelocityClass;
import com.coupang.product.domain.model.fcproduct.WarehouseId;
import com.coupang.product.domain.port.FCProductQueryPort;
import com.coupang.product.domain.repository.FCProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class FCProductQueryService implements FCProductQueryPort {

    private final FCProductRepository fcProductRepository;

    public FCProductQueryService(FCProductRepository fcProductRepository) {
        this.fcProductRepository = fcProductRepository;
    }

    public Optional<FCProduct> getFCProduct(String fcProductId) {
        return fcProductRepository.findById(fcProductId);
    }

    public Optional<FCProduct> getFCProductBySkuAndWarehouse(String sku, String warehouseId) {
        return fcProductRepository.findBySkuAndWarehouseId(sku, warehouseId);
    }

    public List<FCProduct> getFCProductsByWarehouse(String warehouseId, FCProductStatus status) {
        return fcProductRepository.findByWarehouseIdAndStatus(
                new WarehouseId(warehouseId), status);
    }

    public List<FCProduct> getFCsByProduct(String sku) {
        return fcProductRepository.findBySku(sku);
    }

    public List<FCProduct> getFCProductsByVelocity(String warehouseId, VelocityClass velocity) {
        return fcProductRepository.findByWarehouseIdAndVelocity(warehouseId, velocity);
    }
}
