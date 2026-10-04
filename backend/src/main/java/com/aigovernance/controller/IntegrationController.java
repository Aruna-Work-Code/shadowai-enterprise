package com.aigovernance.controller;

import com.aigovernance.model.IntegrationConnection;
import com.aigovernance.repository.IntegrationConnectionRepository;
import com.aigovernance.service.IntegrationService;
import org.springframework.security.access.prepost.PreAuthorize;
import com.aigovernance.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Administration API for enterprise integrations.
 *
 * Phase 8 responsibilities:
 *
 * - List integrations
 * - Create integrations
 * - Enable integrations
 * - Disable integrations
 */
@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

    private final IntegrationService service;
    private final IntegrationConnectionRepository repository;

    public IntegrationController(
            IntegrationService service,
            IntegrationConnectionRepository repository
    ) {
        this.service = service;
        this.repository = repository;
    }

    /**
     * List configured integrations.
     *
     * Secret hashes are NEVER exposed.
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public List<IntegrationConnection> all() {

        return repository.findByTenantId(TenantContext.currentOrDefault());
    }

    /**
     * Create a new integration.
     *
     * Example:
     *
     * POST /api/v1/integrations
     *
     * ?name=Security Feed
     * &provider=GENERIC_WEBHOOK
     * &secret=demo-secret
     * &actor=2
     */
    @PostMapping
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public IntegrationConnection create(
            @RequestParam String name,
            @RequestParam String provider,
            @RequestParam String secret,
            @RequestParam Long actor
    ) {

        return service.create(
                name,
                provider,
                secret,
                actor
        );
    }

    /**
     * Enable an integration.
     */
    @PostMapping("/{id}/enable")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public IntegrationConnection enable(
            @PathVariable Long id
    ) {

        IntegrationConnection connection =
                repository.findByTenantIdAndId(TenantContext.currentOrDefault(),id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Integration not found"
                                )
                        );

        connection.enabled = true;

        return repository.save(connection);
    }

    /**
     * Disable an integration.
     */
    @PostMapping("/{id}/disable")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public IntegrationConnection disable(
            @PathVariable Long id
    ) {

        IntegrationConnection connection =
                repository.findByTenantIdAndId(TenantContext.currentOrDefault(),id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Integration not found"
                                )
                        );

        connection.enabled = false;

        return repository.save(connection);
    }
}