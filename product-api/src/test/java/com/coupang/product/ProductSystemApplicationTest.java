package com.coupang.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test verifying the Spring application context can load.
 *
 * External dependencies (PostgreSQL, Kafka) are not available in test.
 * H2 is used in-memory, and Kafka points to a dummy address (template
 * bean is created but no actual connection is made during context load).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
        // Use H2 instead of PostgreSQL
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        // JPA / Hibernate
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // Disable Flyway
        "spring.flyway.enabled=false",
        // Kafka — dummy address; KafkaTemplate is created but does not connect at startup
        "spring.kafka.bootstrap-servers=localhost:19092",
        // Disable batch scheduling
        "batch.enabled=false"
})
class ProductSystemApplicationTest {

    @Test
    void contextLoads() {
    }
}
