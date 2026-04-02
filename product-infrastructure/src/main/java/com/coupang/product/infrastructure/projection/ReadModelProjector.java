package com.coupang.product.infrastructure.projection;

import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.model.product.Product;
import com.coupang.product.domain.model.product.SellerOffer;
import com.coupang.product.domain.repository.FCProductRepository;
import com.coupang.product.domain.repository.ProductRepository;
import com.coupang.product.domain.rule.ProductRuleService;
import com.coupang.product.domain.rule.SlottingRuleResult;
import com.coupang.product.infrastructure.persistence.readmodel.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Read Model Projector.
 *
 * 도메인 이벤트(Kafka)를 소비하여 Read Model을 비동기 갱신한다.
 * Write Model(Aggregate)과 Read Model이 완전히 분리.
 *
 * ┌─────────────────┐     Kafka      ┌──────────────────────┐
 * │  Write Model    │ ──────────────→│   Projector          │
 * │  (Aggregate)    │  domain events │   (이 클래스)         │
 * │  ProductCommand │                │                      │
 * │  Service 등     │                │  ┌─ OrderProductView │
 * └─────────────────┘                │  ├─ PickingProductView│
 *                                    │  └─ DashboardView    │
 *                                    └──────────────────────┘
 *                                              │
 *                                    ┌─────────┴──────────┐
 *                                    │   Read DB          │
 *                                    │   (별도 테이블)     │
 *                                    └────────────────────┘
 *
 * 각 소비자 시스템은 Read Model만 조회하면 된다:
 * - 주문 시스템 → OrderProductView (단일 쿼리로 판매 가능 여부 + 가격 + FC)
 * - 피킹 시스템 → PickingProductView (단일 쿼리로 지번 + velocity + 취급규칙)
 * - 운영 대시보드 → DashboardProductView (FC별 통계)
 */
@Component
public class ReadModelProjector {

    private static final Logger log = LoggerFactory.getLogger(ReadModelProjector.class);

    private final ProductRepository productRepository;
    private final FCProductRepository fcProductRepository;
    private final OrderProductViewRepository orderViewRepo;
    private final PickingProductViewRepository pickingViewRepo;
    private final DashboardProductViewRepository dashboardViewRepo;
    private final ProductRuleService ruleService;
    private final ObjectMapper objectMapper;

    public ReadModelProjector(
            ProductRepository productRepository,
            FCProductRepository fcProductRepository,
            OrderProductViewRepository orderViewRepo,
            PickingProductViewRepository pickingViewRepo,
            DashboardProductViewRepository dashboardViewRepo,
            ProductRuleService ruleService,
            ObjectMapper objectMapper
    ) {
        this.productRepository = productRepository;
        this.fcProductRepository = fcProductRepository;
        this.orderViewRepo = orderViewRepo;
        this.pickingViewRepo = pickingViewRepo;
        this.dashboardViewRepo = dashboardViewRepo;
        this.ruleService = ruleService;
        this.objectMapper = objectMapper;
    }

    // ═══════════════════════════════════════
    //  Product 이벤트 → OrderProductView 갱신
    // ═══════════════════════════════════════

    /**
     * product.registered, product.updated, product.seller-offer-changed
     * → OrderProductView 생성/갱신
     */
    @KafkaListener(
            topics = {"${kafka.topics.product-registered}",
                      "${kafka.topics.product-updated}",
                      "${kafka.topics.product-seller-offer-changed}"},
            groupId = "product-system-projector-order"
    )
    public void projectOrderView(@Payload String message, Acknowledgment ack) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String sku = json.get("sku").path("value").asText(
                    json.get("sku").asText());

            Optional<Product> productOpt = productRepository.findBySku(sku);
            if (productOpt.isEmpty()) {
                ack.acknowledge();
                return;
            }

            Product product = productOpt.get();
            List<FCProduct> fcProducts = fcProductRepository.findBySku(sku);

            OrderProductView view = orderViewRepo.findById(sku)
                    .orElse(new OrderProductView());

            view.setSku(sku);
            view.setProductName(product.getName());
            view.setCategoryCode(product.getCategory().code());
            view.setProductStatus(product.getStatus().name());

            // 아이템 위너
            Optional<SellerOffer> winner = product.getCurrentWinner();
            winner.ifPresent(w -> {
                view.setWinnerSellerId(w.getSellerId().value());
                view.setWinnerPrice(w.getSellingPrice().amount());
                view.setWinnerPriceCurrency(w.getSellingPrice().currency());
            });

            // FC 가용성
            List<FCProduct> activeFCs = fcProducts.stream()
                    .filter(fp -> fp.getStatus().isOperational())
                    .toList();
            view.setActiveFCCount(activeFCs.size());
            view.setActiveFCList(activeFCs.stream()
                    .map(fp -> fp.getWarehouseId().value())
                    .collect(Collectors.joining(",")));
            view.setHasColdChainFC(activeFCs.stream()
                    .anyMatch(fp -> fp.getStorageProfile().requiresColdChain()));

            // 규격
            view.setLengthMm(product.getDimensions().lengthMm());
            view.setWidthMm(product.getDimensions().widthMm());
            view.setHeightMm(product.getDimensions().heightMm());
            view.setWeightG(product.getDimensions().weightG());
            view.setTemperatureSensitive(product.isTemperatureSensitive());
            view.setOversized(product.getDimensions().isOversized());

            // 판매 가능 여부
            view.setPurchasable(
                    product.getStatus() == com.coupang.product.domain.model.product.ProductStatus.ACTIVE
                    && activeFCs.size() > 0
                    && winner.isPresent()
            );

            view.setLastUpdated(LocalDateTime.now());
            orderViewRepo.save(view);

            log.debug("[Projector] OrderView 갱신: SKU={}, purchasable={}", sku, view.isPurchasable());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Projector] OrderView 갱신 실패", e);
        }
    }

    // ═══════════════════════════════════════
    //  FCProduct 이벤트 → PickingProductView + DashboardView 갱신
    // ═══════════════════════════════════════

    /**
     * fcproduct.registered, fcproduct.slotting-assigned,
     * fcproduct.velocity-reclassified, fcproduct.suspended
     * → PickingProductView + DashboardProductView 갱신
     */
    @KafkaListener(
            topics = {"${kafka.topics.fcproduct-registered}",
                      "${kafka.topics.fcproduct-slotting-assigned}",
                      "${kafka.topics.fcproduct-velocity-reclassified}",
                      "${kafka.topics.fcproduct-suspended}"},
            groupId = "product-system-projector-fc"
    )
    public void projectFCProductViews(@Payload String message, Acknowledgment ack) {
        try {
            JsonNode json = objectMapper.readTree(message);
            String fcProductId = json.get("fcProductId").path("value").asText(
                    json.get("fcProductId").asText());

            Optional<FCProduct> fcProductOpt = fcProductRepository.findById(fcProductId);
            if (fcProductOpt.isEmpty()) {
                ack.acknowledge();
                return;
            }

            FCProduct fp = fcProductOpt.get();
            projectPickingView(fp);
            projectDashboardView(fp);

            // FCProduct 변경은 OrderView의 activeFCCount에도 영향
            // → OrderView도 갱신
            rebuildOrderViewForSku(fp.getSku().value());

            ack.acknowledge();
        } catch (Exception e) {
            log.error("[Projector] FCProduct 뷰 갱신 실패", e);
        }
    }

    private void projectPickingView(FCProduct fp) {
        PickingProductView view = pickingViewRepo.findById(fp.getId().value())
                .orElse(new PickingProductView());

        view.setFcProductId(fp.getId().value());
        view.setSku(fp.getSku().value());
        view.setWarehouseId(fp.getWarehouseId().value());
        view.setStatus(fp.getStatus().name());
        view.setOperational(fp.getStatus().isOperational());

        if (fp.getSlottingInfo() != null) {
            view.setPrimaryLocation(fp.getSlottingInfo().primaryLocation().value());
            view.setReplenishLocation(fp.getSlottingInfo().replenishLocation().value());
            view.setBulkLocation(fp.getSlottingInfo().bulkLocation() != null ?
                    fp.getSlottingInfo().bulkLocation().value() : null);
            view.setPickFaceCapacity(fp.getSlottingInfo().pickFaceCapacity());
        }

        view.setVelocity(fp.getVelocity().name());
        view.setFifo(fp.getHandlingRule().fifo());
        view.setExpiryManaged(fp.getHandlingRule().expiryManaged());
        view.setFragile(fp.getHandlingRule().fragile());
        view.setReplenishTriggerPoint(fp.getReplenishmentPolicy().triggerPoint());
        view.setReplenishMaxQty(fp.getReplenishmentPolicy().maxQty());
        view.setReplenishUnit(fp.getReplenishmentPolicy().replenishUnit().name());
        view.setTemperatureZone(fp.getStorageProfile().temperatureZone().name());
        view.setWeightG(fp.getDimensions().weightG());
        view.setOversized(fp.getDimensions().isOversized());
        view.setLastUpdated(LocalDateTime.now());

        pickingViewRepo.save(view);
        log.debug("[Projector] PickingView 갱신: fcProductId={}", fp.getId().value());
    }

    private void projectDashboardView(FCProduct fp) {
        DashboardProductView view = dashboardViewRepo.findById(fp.getId().value())
                .orElse(new DashboardProductView());

        view.setFcProductId(fp.getId().value());
        view.setSku(fp.getSku().value());
        view.setWarehouseId(fp.getWarehouseId().value());
        view.setStatus(fp.getStatus().name());
        view.setTemperatureZone(fp.getStorageProfile().temperatureZone().name());
        view.setStorageType(fp.getStorageProfile().storageType().name());
        view.setVelocity(fp.getVelocity().name());
        view.setReplenishTriggerPoint(fp.getReplenishmentPolicy().triggerPoint());
        view.setReplenishMaxQty(fp.getReplenishmentPolicy().maxQty());
        view.setFulfillmentTypes(fp.getFulfillmentTypes().stream()
                .map(Enum::name).collect(Collectors.joining(",")));
        view.setRegisteredAt(fp.getRegisteredAt());
        view.setActivatedAt(fp.getActivatedAt());

        if (fp.getSlottingInfo() != null) {
            view.setPrimaryLocation(fp.getSlottingInfo().primaryLocation().value());
        }

        // Rule Engine으로 슬로팅 규칙 평가 결과를 Read Model에 포함
        try {
            SlottingRuleResult slottingResult = ruleService.evaluateSlotting(
                    fp.getVelocity(),
                    fp.getStorageProfile().temperatureZone(),
                    fp.getStorageProfile().storageType(),
                    fp.getDimensions(),
                    fp.getStorageProfile().isHazardous(),
                    fp.getHandlingRule().fragile(),
                    fp.getHandlingRule().expiryManaged()
            );
            view.setSlottingZoneType(slottingResult.zoneType());
            view.setSlottingReason(slottingResult.reason());
        } catch (Exception e) {
            log.warn("슬로팅 규칙 평가 실패: {}", fp.getId().value(), e);
        }

        // 상품명은 Product에서 가져옴
        productRepository.findBySku(fp.getSku().value())
                .ifPresent(p -> {
                    view.setProductName(p.getName());
                    view.setCategoryCode(p.getCategory().code());
                });

        view.setLastUpdated(LocalDateTime.now());
        dashboardViewRepo.save(view);
        log.debug("[Projector] DashboardView 갱신: fcProductId={}", fp.getId().value());
    }

    private void rebuildOrderViewForSku(String sku) {
        // OrderView를 SKU 기준으로 재구성
        productRepository.findBySku(sku).ifPresent(product -> {
            List<FCProduct> fcProducts = fcProductRepository.findBySku(sku);
            List<FCProduct> activeFCs = fcProducts.stream()
                    .filter(fp -> fp.getStatus().isOperational()).toList();

            orderViewRepo.findById(sku).ifPresent(view -> {
                view.setActiveFCCount(activeFCs.size());
                view.setActiveFCList(activeFCs.stream()
                        .map(fp -> fp.getWarehouseId().value())
                        .collect(Collectors.joining(",")));
                view.setPurchasable(
                        product.getStatus() == com.coupang.product.domain.model.product.ProductStatus.ACTIVE
                        && activeFCs.size() > 0
                        && product.getCurrentWinner().isPresent()
                );
                view.setLastUpdated(LocalDateTime.now());
                orderViewRepo.save(view);
            });
        });
    }
}
