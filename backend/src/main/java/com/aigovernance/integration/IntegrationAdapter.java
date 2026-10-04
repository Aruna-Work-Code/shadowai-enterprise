package com.aigovernance.integration;

import com.aigovernance.model.IntegrationConnection;

import java.util.Map;

public interface IntegrationAdapter {

    String provider();

    boolean supports(String provider);

    boolean validate(IntegrationConnection connection);

    IntegrationTestResult test(
            IntegrationConnection connection
    );

    default Map<String, Object> metadata() {
        return Map.of(
                "provider", provider(),
                "type", "GENERIC"
        );
    }
}