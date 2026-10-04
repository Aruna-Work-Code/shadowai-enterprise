package com.aigovernance.integration;

import com.aigovernance.model.IntegrationConnection;
import org.springframework.stereotype.Component;

@Component
public class GenericWebhookAdapter implements IntegrationAdapter {

    @Override
    public String provider() {
        return "GENERIC_WEBHOOK";
    }

    @Override
    public boolean supports(String provider) {

        return provider != null
                && provider.equalsIgnoreCase(
                        "GENERIC_WEBHOOK"
                );
    }

    @Override
    public boolean validate(
            IntegrationConnection connection
    ) {

        return connection != null
                && connection.enabled
                && connection.webhookSecretHash != null
                && !connection.webhookSecretHash.isBlank();
    }

    @Override
    public IntegrationTestResult test(
            IntegrationConnection connection
    ) {

        if (!validate(connection)) {

            return new IntegrationTestResult(
                    false,
                    "Integration configuration is invalid"
            );
        }

        return new IntegrationTestResult(
                true,
                "Generic webhook integration is configured correctly"
        );
    }
}