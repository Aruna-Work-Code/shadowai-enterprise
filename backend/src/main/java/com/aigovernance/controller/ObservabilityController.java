package com.aigovernance.controller;

import com.aigovernance.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.aigovernance.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/observability")
public class ObservabilityController {

    private final RequestRepository requests;
    private final ExceptionRequestRepository exceptions;
    private final InvestigationRepository investigations;
    private final IntegrationEventRepository events;

    public ObservabilityController(
            RequestRepository requests,
            ExceptionRequestRepository exceptions,
            InvestigationRepository investigations,
            IntegrationEventRepository events) {
        this.requests = requests;
        this.exceptions = exceptions;
        this.investigations = investigations;
        this.events = events;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
    public Map<String, Object> summary() {
        return Map.of(
                "timestamp", Instant.now(),
                "requests", requests.countByTenantId(TenantContext.currentOrDefault()),
                "exceptions", exceptions.findByTenantId(TenantContext.currentOrDefault()).size(),
                "investigations", investigations.findByTenantId(TenantContext.currentOrDefault()).size(),
                "integrationEvents", events.countByTenantId(TenantContext.currentOrDefault()),
                "openInvestigations", investigations.findByTenantIdAndStatus(TenantContext.currentOrDefault(), "OPEN").size(),
                "failedIntegrationEvents", events.findByTenantIdAndProcessingStatus(TenantContext.currentOrDefault(), "FAILED").size()
        );
    }
}
