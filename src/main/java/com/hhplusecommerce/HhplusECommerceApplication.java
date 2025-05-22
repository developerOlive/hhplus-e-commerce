package com.hhplusecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.hhplusecommerce.infrastructure")
@EntityScan(basePackages = "com.hhplusecommerce.domain")
@EnableScheduling
@EnableAsync
public class HhplusECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HhplusECommerceApplication.class, args);
    }

}
