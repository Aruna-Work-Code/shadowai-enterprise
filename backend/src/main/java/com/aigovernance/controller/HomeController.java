package com.aigovernance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Simple service-discovery endpoint. The root URL is intentionally public. */
@RestController
public class HomeController {
    @GetMapping("/")
    public Map<String, Object> home() {
        return Map.of(
                "name", "AI Governance Decision Assistant",
                "version", "1.0.0",
                "status", "running",
                "api", "/api/v1",
                "health", "/actuator/health",
                "docs", "/swagger-ui/index.html"
        );
    }
}
