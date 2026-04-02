package com.coupang.product.application.command;

import com.coupang.product.application.port.outbound.DomainEventPublisher;
import com.coupang.product.domain.exception.DomainException;
import com.coupang.product.domain.model.product.*;
import com.coupang.product.domain.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Service
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final DomainEventPublisher eventPublisher;

    public ProductCommandService(ProductRepository productRepository,
                                  DomainEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public String registerProduct(
            String sku, String name, String description,
            String categoryCode, String categoryDisplayName, String parentCategoryCode,
            int lengthMm, int widthMm, int heightMm, int weightG,
            Set<FulfillmentType> fulfillmentTypes, String barcode,
            boolean temperatureSensitive, boolean hazardous,
            String sellerId, BigDecimal sellingPrice, BigDecimal supplyPrice, int leadTimeDays
    ) {
        if (productRepository.existsBySku(sku)) {
            throw new DomainException("DUPLICATE_SKU", "이미 존재하는 SKU입니다: " + sku);
        }

        Product product = Product.register(
                new SKU(sku), name, description,
                new Category(categoryCode, categoryDisplayName, parentCategoryCode),
                new ProductDimension(lengthMm, widthMm, heightMm, weightG),
                fulfillmentTypes, barcode, temperatureSensitive, hazardous
        );

        if (sellerId != null) {
            product.addSellerOffer(new SellerOffer(
                    new SellerId(sellerId),
                    Price.krw(sellingPrice),
                    Price.krw(supplyPrice),
                    leadTimeDays
            ));
        }

        productRepository.save(product);
        eventPublisher.publishAll(product.getDomainEvents());
        product.clearDomainEvents();
        return product.getId().value();
    }

    /**
     * 상품 승인 (PENDING_REVIEW → ACTIVE).
     */
    @Transactional
    public void approveProduct(String productId) {
        Product product = getProduct(productId);
        product.approve();
        productRepository.save(product);
    }

    /**
     * 상품 정보 변경.
     * → Kafka: product.updated
     * → 소비자: 입고(규격 변경 시 적치 규칙 재검토), 주문(상품명 동기화)
     */
    @Transactional
    public void updateProductInfo(String productId, String name, String description,
                                   String categoryCode, String categoryDisplayName, String parentCategoryCode) {
        Product product = getProduct(productId);
        product.updateInfo(name, description,
                new Category(categoryCode, categoryDisplayName, parentCategoryCode));
        productRepository.save(product);
        eventPublisher.publishAll(product.getDomainEvents());
        product.clearDomainEvents();
    }

    /**
     * 상품 규격 변경.
     * → Kafka: product.updated (dimensions 포함)
     * → 소비자: FC상품관리(슬로팅 재검토), 입고(적치 규칙), 출고(포장 규격)
     */
    @Transactional
    public void updateDimensions(String productId, int lengthMm, int widthMm, int heightMm, int weightG) {
        Product product = getProduct(productId);
        product.updateDimensions(new ProductDimension(lengthMm, widthMm, heightMm, weightG));
        productRepository.save(product);
        eventPublisher.publishAll(product.getDomainEvents());
        product.clearDomainEvents();
    }

    /**
     * 셀러 오퍼 추가.
     * → Kafka: product.seller-offer-changed (위너 변경 포함)
     * → 소비자: 주문(가격/위너 동기화), 발주(공급가 참조)
     */
    @Transactional
    public void addSellerOffer(String productId, String sellerId,
                                BigDecimal sellingPrice, BigDecimal supplyPrice, int leadTimeDays) {
        Product product = getProduct(productId);
        SellerOffer offer = new SellerOffer(
                new SellerId(sellerId),
                Price.krw(sellingPrice),
                Price.krw(supplyPrice),
                leadTimeDays
        );
        product.addSellerOffer(offer);
        productRepository.save(product);
        eventPublisher.publishAll(product.getDomainEvents());
        product.clearDomainEvents();
    }

    /**
     * 상품 단종.
     * → Kafka: product.discontinued
     * → 소비자: 발주(발주 중지), 입고(입고 거부), 재고(잔여 처리), 주문(판매 중지)
     */
    @Transactional
    public void discontinueProduct(String productId) {
        Product product = getProduct(productId);
        product.discontinue();
        productRepository.save(product);
        eventPublisher.publishAll(product.getDomainEvents());
        product.clearDomainEvents();
    }

    private Product getProduct(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new DomainException("PRODUCT_NOT_FOUND",
                        "상품을 찾을 수 없습니다: " + productId));
    }
}
