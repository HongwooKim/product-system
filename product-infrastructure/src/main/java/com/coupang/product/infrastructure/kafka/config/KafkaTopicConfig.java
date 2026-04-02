package com.coupang.product.infrastructure.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    // ── 상품 시스템이 발행하는 토픽 ──

    @Bean
    public NewTopic productRegisteredTopic(
            @Value("${kafka.topics.product-registered}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic productUpdatedTopic(
            @Value("${kafka.topics.product-updated}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic productDiscontinuedTopic(
            @Value("${kafka.topics.product-discontinued}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic sellerOfferChangedTopic(
            @Value("${kafka.topics.product-seller-offer-changed}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic fcProductRegisteredTopic(
            @Value("${kafka.topics.fcproduct-registered}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic fcProductActivatedTopic(
            @Value("${kafka.topics.fcproduct-activated}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic fcProductSlottingAssignedTopic(
            @Value("${kafka.topics.fcproduct-slotting-assigned}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic fcProductVelocityTopic(
            @Value("${kafka.topics.fcproduct-velocity-reclassified}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic fcProductSuspendedTopic(
            @Value("${kafka.topics.fcproduct-suspended}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic replenishmentNeededTopic(
            @Value("${kafka.topics.fcproduct-replenishment-needed}") String topic) {
        return TopicBuilder.name(topic).partitions(6).replicas(3).build();
    }
}
