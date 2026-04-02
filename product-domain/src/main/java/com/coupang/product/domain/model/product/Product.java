package com.coupang.product.domain.model.product;

import com.coupang.product.domain.event.ProductEvents;
import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.common.AggregateRoot;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 상품 Aggregate Root.
 *
 * "이 상품이 무엇인가"를 정의한다.
 * - 상품 마스터 정보 (이름, 카테고리, 규격, 이미지)
 * - 셀러별 판매 오퍼 (가격, 리드타임)
 * - 아이템 위너 선정
 * - 풀필먼트 유형 (로켓배송, 로켓프레시 등)
 *
 * 이 Aggregate는 물류 운영 규칙(슬로팅, 보충 등)을 모른다.
 * 그건 FCProduct의 책임이다.
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_sku", columnList = "sku", unique = true),
        @Index(name = "idx_product_status", columnList = "status"),
        @Index(name = "idx_product_category", columnList = "categoryCode")
})
public class Product extends AggregateRoot {

    @Id
    @Column(length = 36)
    private ProductId id;

    @Column(nullable = false, unique = true, length = 50)
    private SKU sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Category (embedded as flat columns)
    @Column(nullable = false, length = 100)
    private String categoryCode;

    @Column(nullable = false, length = 200)
    private String categoryDisplayName;

    @Column(length = 100)
    private String parentCategoryCode;

    // ProductDimension (embedded as flat columns)
    private int lengthMm;
    private int widthMm;
    private int heightMm;
    private int weightG;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_fulfillment_types", joinColumns = @JoinColumn(name = "product_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_type")
    private Set<FulfillmentType> fulfillmentTypes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private List<SellerOffer> sellerOffers;

    @Column(length = 50)
    private String barcode;

    private String imageUrl;
    private boolean temperatureSensitive;
    private boolean hazardous;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════
    //  Factory Method
    // ═══════════════════════════════════════

    public static Product register(
            SKU sku,
            String name,
            String description,
            Category category,
            ProductDimension dimensions,
            Set<FulfillmentType> fulfillmentTypes,
            String barcode,
            boolean temperatureSensitive,
            boolean hazardous
    ) {
        Product product = new Product();
        product.id = ProductId.generate();
        product.sku = Objects.requireNonNull(sku);
        product.name = validateNotBlank(name, "상품명");
        product.description = description;
        product.setCategory(Objects.requireNonNull(category));
        product.setDimensions(Objects.requireNonNull(dimensions));
        product.fulfillmentTypes = new HashSet<>(fulfillmentTypes);
        product.sellerOffers = new ArrayList<>();
        product.barcode = barcode;
        product.temperatureSensitive = temperatureSensitive;
        product.hazardous = hazardous;
        product.status = ProductStatus.PENDING_REVIEW;
        product.createdAt = LocalDateTime.now();
        product.updatedAt = product.createdAt;

        product.validateFulfillmentTypeConstraints();

        product.raise(new ProductEvents.Registered(
                product.id, product.sku, product.name,
                product.getCategory(), product.getDimensions(), product.fulfillmentTypes,
                product.status, product.temperatureSensitive, product.hazardous
        ));

        return product;
    }

    /**
     * DB에서 복원할 때 사용. 이벤트 발행 없음, 검증 없음.
     */
    public static Product reconstitute(
            ProductId id, SKU sku, String name, String description,
            Category category, ProductDimension dimensions, ProductStatus status,
            Set<FulfillmentType> fulfillmentTypes, List<SellerOffer> sellerOffers,
            String barcode, String imageUrl,
            boolean temperatureSensitive, boolean hazardous,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        Product product = new Product();
        product.id = id;
        product.sku = sku;
        product.name = name;
        product.description = description;
        product.setCategory(category);
        product.setDimensions(dimensions);
        product.status = status;
        product.fulfillmentTypes = new HashSet<>(fulfillmentTypes);
        product.sellerOffers = new ArrayList<>(sellerOffers);
        product.barcode = barcode;
        product.imageUrl = imageUrl;
        product.temperatureSensitive = temperatureSensitive;
        product.hazardous = hazardous;
        product.createdAt = createdAt;
        product.updatedAt = updatedAt;
        return product;
    }

    protected Product() {}

    // ═══════════════════════════════════════
    //  상품 정보 변경
    // ═══════════════════════════════════════

    public void updateInfo(String name, String description, Category category) {
        assertNotDiscontinued();
        this.name = validateNotBlank(name, "상품명");
        this.description = description;
        setCategory(Objects.requireNonNull(category));
        this.updatedAt = LocalDateTime.now();

        raise(new ProductEvents.Updated(id, sku, this.name, getCategory(), getDimensions(), status));
    }

    public void updateDimensions(ProductDimension newDimensions) {
        assertNotDiscontinued();
        setDimensions(Objects.requireNonNull(newDimensions));
        this.updatedAt = LocalDateTime.now();

        // 규격 변경은 FC 상품 관리에 큰 영향 → 이벤트 발행
        raise(new ProductEvents.Updated(id, sku, name, getCategory(), newDimensions, status));
    }

    // ═══════════════════════════════════════
    //  셀러 오퍼 관리 + 아이템 위너
    // ═══════════════════════════════════════

    public void addSellerOffer(SellerOffer offer) {
        assertNotDiscontinued();

        boolean alreadyExists = sellerOffers.stream()
                .anyMatch(o -> o.getSellerId().equals(offer.getSellerId()) && o.isActive());
        if (alreadyExists) {
            throw new DomainException("DUPLICATE_OFFER",
                    "셀러 " + offer.getSellerId() + "의 활성 오퍼가 이미 존재합니다");
        }

        sellerOffers.add(offer);
        recalculateWinner();

        raise(new ProductEvents.SellerOfferChanged(id, sku, getCurrentWinner().orElse(null), status));
    }

    public void updateSellerOffer(SellerId sellerId, Price newSellingPrice, Price newSupplyPrice) {
        assertNotDiscontinued();

        SellerOffer offer = findActiveOffer(sellerId);
        offer.updatePrice(newSellingPrice, newSupplyPrice);
        recalculateWinner();
        this.updatedAt = LocalDateTime.now();

        raise(new ProductEvents.SellerOfferChanged(id, sku, getCurrentWinner().orElse(null), status));
    }

    public void deactivateSellerOffer(SellerId sellerId) {
        SellerOffer offer = findActiveOffer(sellerId);
        offer.deactivate();
        recalculateWinner();
        this.updatedAt = LocalDateTime.now();

        raise(new ProductEvents.SellerOfferChanged(id, sku, getCurrentWinner().orElse(null), status));
    }

    /**
     * 아이템 위너 재산정.
     * 활성 오퍼 중 점수가 가장 높은 셀러가 위너.
     */
    private void recalculateWinner() {
        sellerOffers.forEach(SellerOffer::revokeWinner);

        sellerOffers.stream()
                .filter(SellerOffer::isActive)
                .max(Comparator.comparingDouble(SellerOffer::calculateWinnerScore))
                .ifPresent(SellerOffer::markAsWinner);
    }

    public Optional<SellerOffer> getCurrentWinner() {
        return sellerOffers.stream()
                .filter(SellerOffer::isWinner)
                .findFirst();
    }

    // ═══════════════════════════════════════
    //  상태 전이
    // ═══════════════════════════════════════

    public void approve() {
        assertTransition(ProductStatus.ACTIVE);
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend(String reason) {
        assertTransition(ProductStatus.SUSPENDED);
        this.status = ProductStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reactivate() {
        assertTransition(ProductStatus.ACTIVE);
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void discontinue() {
        assertTransition(ProductStatus.DISCONTINUED);
        this.status = ProductStatus.DISCONTINUED;
        this.updatedAt = LocalDateTime.now();

        // 모든 오퍼 비활성화
        sellerOffers.forEach(SellerOffer::deactivate);

        raise(new ProductEvents.Discontinued(id, sku));
    }

    // ═══════════════════════════════════════
    //  조회용 메서드
    // ═══════════════════════════════════════

    public boolean isRocketFresh() {
        return fulfillmentTypes.contains(FulfillmentType.ROCKET_FRESH);
    }

    public boolean requiresColdChain() {
        return temperatureSensitive || isRocketFresh();
    }

    public List<SellerOffer> getActiveOffers() {
        return sellerOffers.stream()
                .filter(SellerOffer::isActive)
                .collect(Collectors.toUnmodifiableList());
    }

    // ═══════════════════════════════════════
    //  불변식 검증
    // ═══════════════════════════════════════

    private void validateFulfillmentTypeConstraints() {
        if (fulfillmentTypes.contains(FulfillmentType.ROCKET_FRESH) && !temperatureSensitive) {
            throw new DomainException("INVALID_FULFILLMENT",
                    "로켓프레시 상품은 temperatureSensitive=true 여야 합니다");
        }
    }

    private void assertNotDiscontinued() {
        if (status == ProductStatus.DISCONTINUED) {
            throw new DomainException("PRODUCT_DISCONTINUED",
                    "단종된 상품은 수정할 수 없습니다: " + sku);
        }
    }

    private void assertTransition(ProductStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new DomainException("INVALID_STATUS_TRANSITION",
                    "상태 전이 불가: " + status + " → " + target);
        }
    }

    private SellerOffer findActiveOffer(SellerId sellerId) {
        return sellerOffers.stream()
                .filter(o -> o.getSellerId().equals(sellerId) && o.isActive())
                .findFirst()
                .orElseThrow(() -> new DomainException("OFFER_NOT_FOUND",
                        "셀러 " + sellerId + "의 활성 오퍼를 찾을 수 없습니다"));
    }

    private static String validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainException("VALIDATION", fieldName + "은(는) 비어있을 수 없습니다");
        }
        return value;
    }

    // ═══════════════════════════════════════
    //  Category / Dimensions helper (flat column ↔ VO)
    // ═══════════════════════════════════════

    private void setCategory(Category category) {
        this.categoryCode = category.code();
        this.categoryDisplayName = category.displayName();
        this.parentCategoryCode = category.parentCode();
    }

    private void setDimensions(ProductDimension dimensions) {
        this.lengthMm = dimensions.lengthMm();
        this.widthMm = dimensions.widthMm();
        this.heightMm = dimensions.heightMm();
        this.weightG = dimensions.weightG();
    }

    // Getters
    public ProductId getId() { return id; }
    public SKU getSku() { return sku; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return new Category(categoryCode, categoryDisplayName, parentCategoryCode); }
    public ProductDimension getDimensions() { return new ProductDimension(lengthMm, widthMm, heightMm, weightG); }
    public ProductStatus getStatus() { return status; }
    public Set<FulfillmentType> getFulfillmentTypes() { return Collections.unmodifiableSet(fulfillmentTypes); }
    public List<SellerOffer> getSellerOffers() { return Collections.unmodifiableList(sellerOffers); }
    public String getBarcode() { return barcode; }
    public String getImageUrl() { return imageUrl; }
    public boolean isTemperatureSensitive() { return temperatureSensitive; }
    public boolean isHazardous() { return hazardous; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
