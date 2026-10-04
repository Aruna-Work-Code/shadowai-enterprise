package com.aigovernance.integration;

public record IntegrationTestResult(
        boolean success,
        String message
) {
}