package com.coupang.product.application.command;

import com.coupang.product.domain.port.DomainEventPublisher;
import com.coupang.product.domain.port.OutboundDataPort;
import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.fcproduct.*;
import com.coupang.product.domain.model.product.FulfillmentType;
import com.coupang.product.domain.model.product.Product;
import com.coupang.product.domain.model.product.SKU;
import com.coupang.product.domain.repository.FCProductRepository;
import com.coupang.product.domain.repository.ProductRepository;
import com.coupang.product.domain.service.VelocityCalculationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class FCProductCommandService {

    private final FCProductRepository fcProductRepository;
    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;
    private final OutboundDataPort outboundDataPort;

    public FCProductCommandService(FCProductRepository fcProductRepository,
                                    ProductRepository productRepository,
                                    DomainEventPublisher eventPublisher,
                                    OutboundDataPort outboundDataPort) {
        this.fcProductRepository = fcProductRepository;
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
        this.outboundDataPort = outboundDataPort;
    }

    @Transactional
    public String registerFCProduct(
            String sku, String warehouseId,
            TemperatureZone temperatureZone, StorageType storageType,
            int maxStackHeight, String hazardClass,
            boolean fifo, boolean expiryManaged, boolean lotTracking, boolean fragile,
            Set<FulfillmentType> fulfillmentTypes
    ) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new DomainException("PRODUCT_NOT_FOUND",
                        "상품 마스터가 존재하지 않습니다: " + sku));

        if (fcProductRepository.existsBySkuAndWarehouseId(sku, warehouseId)) {
            throw new DomainException("DUPLICATE_FC_PRODUCT",
                    "이미 해당 FC에 등록된 상품입니다: " + sku + " @ " + warehouseId);
        }

        FCProduct fcProduct = FCProduct.register(
                new SKU(sku),
                new WarehouseId(warehouseId),
                product.getDimensions(),
                new StorageProfile(temperatureZone, storageType, maxStackHeight, hazardClass),
                new HandlingRule(fifo, expiryManaged, lotTracking, fragile),
                fulfillmentTypes
        );

        fcProductRepository.save(fcProduct);
        eventPublisher.publishAll(fcProduct.getDomainEvents());
        fcProduct.clearDomainEvents();
        return fcProduct.getId().value();
    }

    @Transactional
    public void assignSlotting(
            String fcProductId,
            String primaryLocation, String replenishLocation,
            String bulkLocation, int pickFaceCapacity
    ) {
        FCProduct fcProduct = getFCProduct(fcProductId);
        fcProduct.startSlotting();
        fcProduct.assignSlotting(new SlottingInfo(
                new LocationCode(primaryLocation),
                new LocationCode(replenishLocation),
                bulkLocation != null ? new LocationCode(bulkLocation) : null,
                pickFaceCapacity
        ));
        fcProductRepository.save(fcProduct);
        eventPublisher.publishAll(fcProduct.getDomainEvents());
        fcProduct.clearDomainEvents();
    }

    /**
     * 재슬로팅.
     * → Kafka: fcproduct.reslotted
     * → 소비자: 재고(위치 변경), 피킹(지번 갱신)
     */
    @Transactional
    public void reslot(String fcProductId, String primaryLocation,
                       String replenishLocation, String bulkLocation,
                       int pickFaceCapacity, String reason) {
        FCProduct fcProduct = getFCProduct(fcProductId);

        SlottingInfo newSlotting = new SlottingInfo(
                new LocationCode(primaryLocation),
                new LocationCode(replenishLocation),
                bulkLocation != null ? new LocationCode(bulkLocation) : null,
                pickFaceCapacity
        );

        fcProduct.reslot(newSlotting, reason);
        fcProductRepository.save(fcProduct);
        eventPublisher.publishAll(fcProduct.getDomainEvents());
        fcProduct.clearDomainEvents();
    }

    /**
     * Velocity 전체 재산정 (일 배치).
     * 피킹 시스템의 출고 실적 데이터를 기반으로 전 FC 상품의 등급을 재산정.
     *
     * → Kafka: fcproduct.velocity-reclassified (변경된 상품마다)
     * → 소비자: 피킹(우선순위 갱신), 재슬로팅 배치 트리거
     */
    @Transactional
    public int recalculateVelocity(String warehouseId) {
        // Application 레이어가 외부 데이터를 가져와서 도메인 서비스에 주입
        List<FCProduct> activeProducts = fcProductRepository
                .findByWarehouseIdAndStatus(new WarehouseId(warehouseId), FCProductStatus.ACTIVE);
        Map<String, Integer> outboundCounts = outboundDataPort
                .getOutboundCounts(warehouseId, 28);

        VelocityCalculationService velocityService = new VelocityCalculationService();
        List<FCProduct> changed = velocityService.recalculateAll(activeProducts, outboundCounts);

        for (FCProduct product : changed) {
            fcProductRepository.save(product);
            eventPublisher.publishAll(product.getDomainEvents());
            product.clearDomainEvents();
        }

        return changed.size();
    }

    /**
     * FC 상품 정지.
     * → Kafka: fcproduct.suspended
     * → 소비자: 입고(입고 거부), 피킹(피킹 제외), 출고(출고 보류), 주문(판매 불가)
     */
    @Transactional
    public void suspendFCProduct(String fcProductId, SuspensionReason reason) {
        FCProduct fcProduct = getFCProduct(fcProductId);
        fcProduct.suspend(reason);
        fcProductRepository.save(fcProduct);
        eventPublisher.publishAll(fcProduct.getDomainEvents());
        fcProduct.clearDomainEvents();
    }

    /**
     * FC 상품 재활성화.
     */
    @Transactional
    public void reactivateFCProduct(String fcProductId) {
        FCProduct fcProduct = getFCProduct(fcProductId);
        fcProduct.reactivate();
        fcProductRepository.save(fcProduct);
    }

    /**
     * FC 상품 해제(단종).
     * → Kafka: fcproduct.discontinued
     * → 소비자: 입고(입고 거부), 재고(잔여 재고 이관/폐기), 피킹(제외)
     */
    @Transactional
    public void discontinueFCProduct(String fcProductId) {
        FCProduct fcProduct = getFCProduct(fcProductId);
        fcProduct.discontinue();
        fcProductRepository.save(fcProduct);
        eventPublisher.publishAll(fcProduct.getDomainEvents());
        fcProduct.clearDomainEvents();
    }

    /**
     * 보충 필요 여부 평가.
     * 재고 시스템의 stock-deducted 이벤트에서 호출.
     * → Kafka: fcproduct.replenishment-needed (보충 필요 시)
     */
    @Transactional(readOnly = true)
    public void evaluateReplenishment(String fcProductId, int currentPickFaceQty) {
        fcProductRepository.findById(fcProductId).ifPresent(product -> {
            if (!product.getStatus().isOperational()) return;
            if (!product.needsReplenishment(currentPickFaceQty)) return;

            FCProduct.ReplenishmentSpec spec = product.buildReplenishmentSpec(currentPickFaceQty);
            eventPublisher.publish(new com.coupang.product.domain.event.ReplenishmentNeededEvent(spec));
        });
    }

    private FCProduct getFCProduct(String fcProductId) {
        return fcProductRepository.findById(fcProductId)
                .orElseThrow(() -> new DomainException("FC_PRODUCT_NOT_FOUND",
                        "FC 상품을 찾을 수 없습니다: " + fcProductId));
    }
}
