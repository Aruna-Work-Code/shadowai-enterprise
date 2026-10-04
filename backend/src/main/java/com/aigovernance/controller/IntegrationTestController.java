package com.aigovernance.controller;

import com.aigovernance.integration.IntegrationAdapter;
import com.aigovernance.integration.IntegrationAdapterRegistry;
import com.aigovernance.integration.IntegrationTestResult;
import com.aigovernance.model.IntegrationConnection;
import com.aigovernance.repository.IntegrationConnectionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import com.aigovernance.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Integration provider discovery and health testing.
 */
@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationTestController {

    private final IntegrationConnectionRepository connections;
    private final IntegrationAdapterRegistry registry;

    public IntegrationTestController(
            IntegrationConnectionRepository connections,
            IntegrationAdapterRegistry registry
    ) {
        this.connections = connections;
        this.registry = registry;
    }

    /**
     * List supported integration providers.
     */
    @GetMapping("/providers")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public List<Map<String, Object>> providers() {

        return registry.all()
                .stream()
                .map(IntegrationAdapter::metadata)
                .toList();
    }

    /**
     * Test a configured integration.
     */
    @PostMapping("/{id}/test")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public IntegrationTestResult test(
            @PathVariable Long id
    ) {

        IntegrationConnection connection =
                connections.findByTenantIdAndId(TenantContext.currentOrDefault(),id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Integration not found"
                                )
                        );

        IntegrationAdapter adapter =
                registry.get(connection.provider);

        return adapter.test(connection);
    }
}