package com.coupang.product.infrastructure.persistence.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductViewRepository extends JpaRepository<OrderProductView, String> {
}
