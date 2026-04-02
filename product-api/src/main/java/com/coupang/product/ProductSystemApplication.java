package com.coupang.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProductSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductSystemApplication.class, args);
    }
}
