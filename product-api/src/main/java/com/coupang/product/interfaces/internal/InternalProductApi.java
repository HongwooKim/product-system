package com.coupang.product.interfaces.internal;

import com.coupang.product.application.query.FCProductQueryService;
import com.coupang.product.application.query.ProductQueryService;
import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.model.fcproduct.FCProductStatus;
import com.coupang.product.domain.model.product.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 내부 시스템 전용 API.
 * 주문/입고/피킹/출고/발주/배송 시스템이 상품 정보를 실시간 조회.
 *
 * 외부 고객 API(ProductController)와 분리하여:
 * - 인증 체계가 다름 (내부 mTLS / 서비스 토큰)
 * - 응답 형식이 간결함 (내부 시스템이 필요한 필드만)
 * - SLA가 다름 (더 높은 가용성 요구)
 *
 * ═══════════════════════════════════════════════════════════
 * 소비자별 사용 시나리오:
 *
 * [주문 시스템]
 *   - 주문 생성 시 상품 판매 가능 여부 확인
 *   - SKU → 활성 FC 목록 조회 → 배송지 기반 FC 선택
 *   - 아이템 위너(가격) 조회
 *
 * [입고 시스템]
 *   - ASN 생성 시 FCProduct 존재 여부 확인
 *   - 적치 시 StorageProfile/HandlingRule 조회
 *   - 슬로팅 정보(피킹 지번, 보충 지번) 조회
 *
 * [피킹 시스템]
 *   - 피킹 Task 생성 시 피킹 지번 조회
 *   - 보충 Task 생성 시 보충 지번 + FIFO 여부 조회
 *   - Velocity 등급 조회 (피킹 우선순위)
 *
 * [출고 시스템]
 *   - 포장 시 상품 규격(dimensions) 조회 → 박스 사이즈 결정
 *   - 취급주의(fragile) 여부 조회
 *
 * [발주 시스템]
 *   - 발주 생성 시 상품 마스터 유효성 확인
 *   - FC별 등록 상태 확인 (발주 → 입고할 FC 결정)
 *
 * [배송 시스템]
 *   - 배송 시 상품 규격/무게 조회 → 차량 적재 계획
 *   - 온도대 조회 → 콜드체인 차량 배정
 * ═══════════════════════════════════════════════════════════
 */
@RestController
@RequestMapping("/internal/v1")
public class InternalProductApi {

    private final ProductQueryService productQueryService;
    private final FCProductQueryService fcProductQueryService;

    public InternalProductApi(ProductQueryService productQueryService,
                               FCProductQueryService fcProductQueryService) {
        this.productQueryService = productQueryService;
        this.fcProductQueryService = fcProductQueryService;
    }

    // ═══════════════════════════════════════
    //  주문 시스템용
    // ═══════════════════════════════════════

    /**
     * 주문 시 상품 판매 가능 여부 + 활성 FC 목록 조회.
     * 주문 시스템이 배송지 기반으로 FC를 선택할 때 사용.
     *
     * GET /internal/v1/availability?sku=SKU-12345
     */
    @GetMapping("/availability")
    public ResponseEntity<ProductAvailability> checkAvailability(@RequestParam String sku) {
        return productQueryService.getProductBySku(sku)
                .map(product -> {
                    List<FCProduct> fcProducts = fcProductQueryService.getFCsByProduct(sku);
                    List<FCAvailabilityInfo> activeFCs = fcProducts.stream()
                            .filter(fp -> fp.getStatus().isOperational())
                            .map(fp -> new FCAvailabilityInfo(
                                    fp.getWarehouseId().value(),
                                    fp.getStorageProfile().temperatureZone().name(),
                                    fp.getVelocity().name()
                            ))
                            .toList();

                    return ResponseEntity.ok(new ProductAvailability(
                            sku,
                            product.getStatus().name(),
                            product.getStatus() == com.coupang.product.domain.model.product.ProductStatus.ACTIVE,
                            activeFCs.size(),
                            activeFCs,
                            product.getCurrentWinner()
                                    .map(w -> new WinnerInfo(
                                            w.getSellerId().value(),
                                            w.getSellingPrice().amount().toString()))
                                    .orElse(null)
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════
    //  입고/피킹 시스템용
    // ═══════════════════════════════════════

    /**
     * FC 상품의 운영 정보 조회.
     * 입고 시스템: StorageProfile, HandlingRule → 적치 규칙
     * 피킹 시스템: SlottingInfo, Velocity → 피킹 경로/우선순위
     *
     * GET /internal/v1/fc-operations?sku=SKU-12345&warehouseId=FC-PYEONGTAEK
     */
    @GetMapping("/fc-operations")
    public ResponseEntity<FCOperationsInfo> getFCOperations(
            @RequestParam String sku,
            @RequestParam String warehouseId) {
        return fcProductQueryService.getFCProductBySkuAndWarehouse(sku, warehouseId)
                .map(fp -> ResponseEntity.ok(new FCOperationsInfo(
                        fp.getId().value(),
                        fp.getSku().value(),
                        fp.getWarehouseId().value(),
                        fp.getStatus().name(),
                        fp.getStatus().isOperational(),
                        new StorageInfo(
                                fp.getStorageProfile().temperatureZone().name(),
                                fp.getStorageProfile().storageType().name(),
                                fp.getStorageProfile().maxStackHeight(),
                                fp.getStorageProfile().requiresColdChain()
                        ),
                        new HandlingInfo(
                                fp.getHandlingRule().fifo(),
                                fp.getHandlingRule().expiryManaged(),
                                fp.getHandlingRule().lotTracking(),
                                fp.getHandlingRule().fragile()
                        ),
                        fp.getSlottingInfo() != null ? new SlottingDetail(
                                fp.getSlottingInfo().primaryLocation().value(),
                                fp.getSlottingInfo().replenishLocation().value(),
                                fp.getSlottingInfo().bulkLocation() != null ?
                                        fp.getSlottingInfo().bulkLocation().value() : null,
                                fp.getSlottingInfo().pickFaceCapacity()
                        ) : null,
                        fp.getVelocity().name(),
                        new ReplenishInfo(
                                fp.getReplenishmentPolicy().minQty(),
                                fp.getReplenishmentPolicy().maxQty(),
                                fp.getReplenishmentPolicy().triggerPoint(),
                                fp.getReplenishmentPolicy().replenishUnit().name()
                        )
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════
    //  출고/배송 시스템용
    // ═══════════════════════════════════════

    /**
     * 상품 물리 규격 조회.
     * 출고 시스템: 포장 박스 사이즈 결정
     * 배송 시스템: 차량 적재 계획, 콜드체인 차량 배정
     *
     * GET /internal/v1/dimensions?sku=SKU-12345
     */
    @GetMapping("/dimensions")
    public ResponseEntity<DimensionsInfo> getDimensions(@RequestParam String sku) {
        return productQueryService.getProductBySku(sku)
                .map(product -> ResponseEntity.ok(new DimensionsInfo(
                        sku,
                        product.getDimensions().lengthMm(),
                        product.getDimensions().widthMm(),
                        product.getDimensions().heightMm(),
                        product.getDimensions().weightG(),
                        product.getDimensions().cubicVolumeCm3(),
                        product.isTemperatureSensitive(),
                        product.isHazardous(),
                        product.getDimensions().isOversized()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 벌크 조회 — 여러 SKU의 FC 운영 상태를 한 번에 조회.
     * 주문 시스템에서 장바구니 내 여러 상품의 FC 가용성을 한 번에 확인.
     *
     * POST /internal/v1/bulk-availability
     * Body: {"skus": ["SKU-001", "SKU-002", ...]}
     */
    @PostMapping("/bulk-availability")
    public ResponseEntity<List<ProductAvailability>> bulkCheckAvailability(
            @RequestBody Map<String, List<String>> request) {
        List<String> skus = request.getOrDefault("skus", List.of());

        List<ProductAvailability> results = skus.stream()
                .map(sku -> {
                    Product product = productQueryService.getProductBySku(sku).orElse(null);
                    if (product == null) {
                        return new ProductAvailability(sku, "NOT_FOUND", false, 0, List.of(), null);
                    }
                    List<FCAvailabilityInfo> activeFCs = fcProductQueryService.getFCsByProduct(sku).stream()
                            .filter(fp -> fp.getStatus().isOperational())
                            .map(fp -> new FCAvailabilityInfo(
                                    fp.getWarehouseId().value(),
                                    fp.getStorageProfile().temperatureZone().name(),
                                    fp.getVelocity().name()))
                            .toList();
                    return new ProductAvailability(
                            sku, product.getStatus().name(),
                            product.getStatus() == com.coupang.product.domain.model.product.ProductStatus.ACTIVE,
                            activeFCs.size(), activeFCs, null);
                })
                .toList();

        return ResponseEntity.ok(results);
    }

    /**
     * FC별 전체 ACTIVE 상품 수 요약.
     * 발주 시스템에서 FC 용량 판단 시 참고.
     *
     * GET /internal/v1/fc-summary?warehouseId=FC-PYEONGTAEK
     */
    @GetMapping("/fc-summary")
    public ResponseEntity<FCSummary> getFCSummary(@RequestParam String warehouseId) {
        List<FCProduct> active = fcProductQueryService.getFCProductsByWarehouse(warehouseId, FCProductStatus.ACTIVE);
        List<FCProduct> suspended = fcProductQueryService.getFCProductsByWarehouse(warehouseId, FCProductStatus.SUSPENDED);

        long coldChainCount = active.stream()
                .filter(fp -> fp.getStorageProfile().requiresColdChain())
                .count();
        long velocityA = active.stream()
                .filter(fp -> fp.getVelocity() == com.coupang.product.domain.model.fcproduct.VelocityClass.A)
                .count();

        return ResponseEntity.ok(new FCSummary(
                warehouseId,
                active.size(),
                suspended.size(),
                coldChainCount,
                velocityA
        ));
    }

    // ═══════════════════════════════════════
    //  Response DTOs (Internal)
    // ═══════════════════════════════════════

    public record ProductAvailability(
            String sku, String status, boolean available,
            int activeFCCount, List<FCAvailabilityInfo> activeFCs,
            WinnerInfo currentWinner) {}

    public record FCAvailabilityInfo(String warehouseId, String temperatureZone, String velocity) {}
    public record WinnerInfo(String sellerId, String sellingPrice) {}

    public record FCOperationsInfo(
            String fcProductId, String sku, String warehouseId,
            String status, boolean operational,
            StorageInfo storage, HandlingInfo handling,
            SlottingDetail slotting, String velocity,
            ReplenishInfo replenishment) {}

    public record StorageInfo(String temperatureZone, String storageType,
                               int maxStackHeight, boolean coldChain) {}
    public record HandlingInfo(boolean fifo, boolean expiryManaged,
                                boolean lotTracking, boolean fragile) {}
    public record SlottingDetail(String primaryLocation, String replenishLocation,
                                  String bulkLocation, int pickFaceCapacity) {}
    public record ReplenishInfo(int minQty, int maxQty, int triggerPoint, String unit) {}

    public record DimensionsInfo(
            String sku, int lengthMm, int widthMm, int heightMm,
            int weightG, long cubicVolumeCm3,
            boolean temperatureSensitive, boolean hazardous, boolean oversized) {}

    public record FCSummary(
            String warehouseId, int activeProductCount, int suspendedProductCount,
            long coldChainCount, long velocityACount) {}
}
