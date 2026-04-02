package com.coupang.product.domain.service;

import com.coupang.product.domain.model.fcproduct.FCProduct;
import com.coupang.product.domain.model.fcproduct.FCProduct.ReplenishmentSpec;
import com.coupang.product.domain.repository.FCProductRepository;

import java.util.Optional;

/**
 * 보충 판단 도메인 서비스.
 * 재고 시스템에서 StockDeducted 이벤트를 수신하면 이 서비스가 호출된다.
 * 피킹 지번 재고가 트리거 이하이면 보충 스펙을 생성한다.
 */
public class ReplenishmentEvaluationService {

    private final FCProductRepository fcProductRepository;

    public ReplenishmentEvaluationService(FCProductRepository fcProductRepository) {
        this.fcProductRepository = fcProductRepository;
    }

    /**
     * 보충 필요 여부 판단 후 보충 스펙 반환.
     *
     * @param fcProductId FC 상품 ID
     * @param currentPickFaceQty 피킹 지번의 현재 재고 수량 (재고 시스템에서 전달)
     * @return 보충이 필요하면 ReplenishmentSpec, 아니면 empty
     */
    public Optional<ReplenishmentSpec> evaluate(String fcProductId, int currentPickFaceQty) {
        FCProduct product = fcProductRepository.findById(fcProductId)
                .orElseThrow(() -> new IllegalArgumentException("FCProduct not found: " + fcProductId));

        if (!product.getStatus().isOperational()) {
            return Optional.empty();
        }

        if (!product.needsReplenishment(currentPickFaceQty)) {
            return Optional.empty();
        }

        return Optional.of(product.buildReplenishmentSpec(currentPickFaceQty));
    }
}
