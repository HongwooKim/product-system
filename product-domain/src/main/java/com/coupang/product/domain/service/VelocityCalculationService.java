package com.coupang.product.domain.service;

import com.coupang.product.domain.model.fcproduct.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Velocity 재산정 도메인 서비스.
 * 28일 롤링 출고 실적 기반으로 FC 전체 상품의 velocity 등급을 재산정한다.
 *
 * 순수 도메인 서비스: 외부 데이터(outboundCounts)는 파라미터로 전달받는다.
 * 호출자(Application Service)가 외부 시스템에서 데이터를 가져와 주입한다.
 */
public class VelocityCalculationService {

    /**
     * FC 전체 상품의 velocity 재산정.
     * 상위 20% = A, 20~50% = B, 50~80% = C, 하위 20% = D
     *
     * @param activeProducts 해당 FC의 ACTIVE 상태 상품 목록
     * @param outboundCounts FCProductId → 출고 횟수 맵 (피킹 시스템에서 수신)
     * @return velocity가 변경된 FCProduct 목록
     */
    public List<FCProduct> recalculateAll(
            List<FCProduct> activeProducts,
            Map<String, Integer> outboundCounts
    ) {
        if (activeProducts.isEmpty()) return Collections.emptyList();

        // 출고 횟수 내림차순 정렬
        List<FCProduct> sorted = activeProducts.stream()
                .sorted(Comparator.comparingInt(
                        (FCProduct p) -> outboundCounts.getOrDefault(p.getId().value(), 0)
                ).reversed())
                .collect(Collectors.toList());

        int total = sorted.size();
        List<FCProduct> changed = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            double percentile = (double) i / total;
            VelocityClass newVelocity;
            if (percentile < 0.20) newVelocity = VelocityClass.A;
            else if (percentile < 0.50) newVelocity = VelocityClass.B;
            else if (percentile < 0.80) newVelocity = VelocityClass.C;
            else newVelocity = VelocityClass.D;

            FCProduct product = sorted.get(i);
            VelocityClass previous = product.getVelocity();
            product.reclassifyVelocity(newVelocity);

            if (previous != newVelocity) {
                changed.add(product);
            }
        }

        return changed;
    }
}
