package com.coupang.product.domain.port.readmodel;

import java.util.List;
import java.util.Optional;

/**
 * CQRS Read Model 조회 포트.
 * product-api(interfaces)가 이 인터페이스를 사용.
 * product-infrastructure가 구현.
 *
 * → product-api가 JPA Repository를 직접 참조하지 않아도 됨.
 */
public interface ReadModelQueryService {

    // ── 주문 시스템용 ──
    Optional<OrderProductViewDto> getOrderView(String sku);

    // ── 피킹 시스템용 ──
    Optional<PickingProductViewDto> getPickingView(String fcProductId);
    List<PickingProductViewDto> getOperationalPickingViews(String warehouseId);
    List<PickingProductViewDto> getPickingViewsByLocation(String location);

    // ── 대시보드용 ──
    List<DashboardProductViewDto> getDashboardByStatus(String warehouseId, String status);
    List<DashboardProductViewDto> getDashboardByVelocity(String warehouseId, String velocity);
    List<Object[]> getVelocityDistribution(String warehouseId);
    List<Object[]> getTemperatureDistribution(String warehouseId);
}
