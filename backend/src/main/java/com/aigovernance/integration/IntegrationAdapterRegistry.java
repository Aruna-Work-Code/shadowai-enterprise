package com.aigovernance.integration;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntegrationAdapterRegistry {

    private final List<IntegrationAdapter> adapters;

    public IntegrationAdapterRegistry(
            List<IntegrationAdapter> adapters
    ) {
        this.adapters = adapters;
    }

    public IntegrationAdapter get(
            String provider
    ) {

        return adapters.stream()
                .filter(adapter ->
                        adapter.supports(provider)
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unsupported integration provider: "
                                        + provider
                        )
                );
    }

    public List<IntegrationAdapter> all() {
        return adapters;
    }
}