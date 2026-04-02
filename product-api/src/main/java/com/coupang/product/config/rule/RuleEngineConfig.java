package com.coupang.product.config.rule;

import com.coupang.product.domain.rule.ProductRuleService;
import com.coupang.product.domain.rule.RuleRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleEngineConfig {

    @Bean
    public ProductRuleService productRuleService(RuleRepository ruleRepository) {
        return new ProductRuleService(ruleRepository);
    }
}
