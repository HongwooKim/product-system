package com.coupang.product.application.batch;

/**
 * 배치 실행 결과. 모든 배치 서비스가 공통으로 반환.
 */
public record BatchResult(
        String warehouseId,
        String batchType,
        int totalProcessed,
        int affectedCount,
        long elapsedMs
) {
    public static BatchResult empty(String warehouseId, String batchType) {
        return new BatchResult(warehouseId, batchType, 0, 0, 0);
    }
}
