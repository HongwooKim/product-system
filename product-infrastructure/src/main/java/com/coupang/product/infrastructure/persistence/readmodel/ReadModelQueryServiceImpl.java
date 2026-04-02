package com.coupang.product.infrastructure.persistence.readmodel;

import com.coupang.product.application.query.readmodel.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReadModelQueryServiceImpl implements ReadModelQueryService {

    private final OrderProductViewRepository orderViewRepo;
    private final PickingProductViewRepository pickingViewRepo;
    private final DashboardProductViewRepository dashboardViewRepo;

    public ReadModelQueryServiceImpl(OrderProductViewRepository orderViewRepo,
                                      PickingProductViewRepository pickingViewRepo,
                                      DashboardProductViewRepository dashboardViewRepo) {
        this.orderViewRepo = orderViewRepo;
        this.pickingViewRepo = pickingViewRepo;
        this.dashboardViewRepo = dashboardViewRepo;
    }

    @Override
    public Optional<OrderProductViewDto> getOrderView(String sku) {
        return orderViewRepo.findById(sku).map(this::toDto);
    }

    @Override
    public Optional<PickingProductViewDto> getPickingView(String fcProductId) {
        return pickingViewRepo.findById(fcProductId).map(this::toDto);
    }

    @Override
    public List<PickingProductViewDto> getOperationalPickingViews(String warehouseId) {
        return pickingViewRepo.findByWarehouseIdAndOperationalTrue(warehouseId)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<PickingProductViewDto> getPickingViewsByLocation(String location) {
        return pickingViewRepo.findByPrimaryLocation(location)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<DashboardProductViewDto> getDashboardByStatus(String warehouseId, String status) {
        return dashboardViewRepo.findByWarehouseIdAndStatus(warehouseId, status)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<DashboardProductViewDto> getDashboardByVelocity(String warehouseId, String velocity) {
        return dashboardViewRepo.findByWarehouseIdAndVelocity(warehouseId, velocity)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<Object[]> getVelocityDistribution(String warehouseId) {
        return dashboardViewRepo.countByWarehouseGroupByVelocity(warehouseId);
    }

    @Override
    public List<Object[]> getTemperatureDistribution(String warehouseId) {
        return dashboardViewRepo.countActiveByWarehouseGroupByTempZone(warehouseId);
    }

    // ── Mapping ──

    private OrderProductViewDto toDto(OrderProductView v) {
        return new OrderProductViewDto(
                v.getSku(), v.getProductName(), v.getCategoryCode(), v.getProductStatus(),
                v.getWinnerSellerId(), v.getWinnerPrice(), v.getWinnerPriceCurrency(),
                v.getActiveFCCount(), v.getActiveFCList(), v.isHasColdChainFC(),
                v.getLengthMm(), v.getWidthMm(), v.getHeightMm(), v.getWeightG(),
                v.isTemperatureSensitive(), v.isOversized(), v.isPurchasable(),
                v.getLastUpdated() != null ? v.getLastUpdated().toString() : null
        );
    }

    private PickingProductViewDto toDto(PickingProductView v) {
        return new PickingProductViewDto(
                v.getFcProductId(), v.getSku(), v.getWarehouseId(), v.getStatus(),
                v.isOperational(), v.getPrimaryLocation(), v.getReplenishLocation(),
                v.getBulkLocation(), v.getPickFaceCapacity(), v.getVelocity(),
                v.isFifo(), v.isExpiryManaged(), v.isFragile(),
                v.getReplenishTriggerPoint(), v.getReplenishMaxQty(), v.getReplenishUnit(),
                v.getTemperatureZone(), v.getWeightG(), v.isOversized(),
                v.getLastUpdated() != null ? v.getLastUpdated().toString() : null
        );
    }

    private DashboardProductViewDto toDto(DashboardProductView v) {
        return new DashboardProductViewDto(
                v.getFcProductId(), v.getSku(), v.getProductName(), v.getWarehouseId(),
                v.getStatus(), v.getCategoryCode(), v.getTemperatureZone(), v.getStorageType(),
                v.getPrimaryLocation(), v.getVelocity(),
                v.getReplenishTriggerPoint(), v.getReplenishMaxQty(),
                v.getSlottingZoneType(), v.getSlottingReason(), v.getFulfillmentTypes(),
                v.getRegisteredAt() != null ? v.getRegisteredAt().toString() : null,
                v.getActivatedAt() != null ? v.getActivatedAt().toString() : null,
                v.getLastUpdated() != null ? v.getLastUpdated().toString() : null
        );
    }
}
