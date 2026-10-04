package com.aigovernance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AiGovernanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                AiGovernanceApplication.class,
                args
        );
    }
}