package com.coupang.product.interfaces.rest;

import com.coupang.product.application.command.ProductCommandService;
import com.coupang.product.application.query.ProductQueryService;
import com.coupang.product.domain.model.product.Product;
import com.coupang.product.domain.model.product.ProductStatus;
import com.coupang.product.interfaces.dto.ProductResponse;
import com.coupang.product.interfaces.dto.RegisterProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductCommandService commandService;
    private final ProductQueryService queryService;

    public ProductController(ProductCommandService commandService, ProductQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    /**
     * 상품 등록.
     * → Kafka: product.registered, product.seller-offer-changed
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> registerProduct(@Valid @RequestBody RegisterProductRequest request) {
        String productId = commandService.registerProduct(
                request.sku(), request.name(), request.description(),
                request.categoryCode(), request.categoryDisplayName(), request.parentCategoryCode(),
                request.lengthMm(), request.widthMm(), request.heightMm(), request.weightG(),
                request.fulfillmentTypes(), request.barcode(),
                request.temperatureSensitive(), request.hazardous(),
                request.sellerId(), request.sellingPrice(), request.supplyPrice(), request.leadTimeDays()
        );
        return ResponseEntity
                .created(URI.create("/api/v1/products/" + productId))
                .body(Map.of("productId", productId));
    }

    /**
     * 상품 상세 조회.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) {
        return queryService.getProduct(productId)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * SKU로 상품 조회.
     */
    @GetMapping(params = "sku")
    public ResponseEntity<ProductResponse> getProductBySku(@RequestParam String sku) {
        return queryService.getProductBySku(sku)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 상태별 상품 목록.
     */
    @GetMapping(params = "status")
    public ResponseEntity<List<ProductResponse>> getProductsByStatus(@RequestParam ProductStatus status) {
        List<ProductResponse> products = queryService.getProductsByStatus(status).stream()
                .map(ProductResponse::from).toList();
        return ResponseEntity.ok(products);
    }

    /**
     * 상품 승인.
     */
    @PostMapping("/{productId}/approve")
    public ResponseEntity<Void> approveProduct(@PathVariable String productId) {
        commandService.approveProduct(productId);
        return ResponseEntity.ok().build();
    }

    /**
     * 상품 단종.
     * → Kafka: product.discontinued
     */
    @PostMapping("/{productId}/discontinue")
    public ResponseEntity<Void> discontinueProduct(@PathVariable String productId) {
        commandService.discontinueProduct(productId);
        return ResponseEntity.ok().build();
    }
}
