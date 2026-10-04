package com.aigovernance.controller;

import com.aigovernance.repository.InvestigationRepository;
import com.aigovernance.repository.RequestRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import com.aigovernance.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final RequestRepository requests;
    private final InvestigationRepository investigations;

    public DashboardController(
            RequestRepository requests,
            InvestigationRepository investigations
    ) {
        this.requests = requests;
        this.investigations = investigations;
    }

    @GetMapping("/summary")
    @PreAuthorize(
            "hasAnyRole('EMPLOYEE','IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')"
    )
    public Map<String, Object> summary() {

        long open =
                investigations.findByTenantIdAndStatus(TenantContext.currentOrDefault(), 
                        "OPEN"
                ).size();

        long pending =
                requests.findByTenantIdAndStatus(TenantContext.currentOrDefault(), 
                        "RECOMMENDED"
                ).size();

        long resolved =
                investigations.findByTenantIdAndStatus(TenantContext.currentOrDefault(), 
                        "RESOLVED"
                ).size();

        int health =
                (int) Math.max(
                        0,
                        Math.min(
                                100,
                                100
                                        - open * 10
                                        - pending * 3
                        )
                );

        return Map.of(
                "governanceHealth",
                health,

                "openInvestigations",
                open,

                "pendingRequests",
                pending,

                "resolvedInvestigations",
                resolved,

                "actionRequired",
                open + pending
        );
    }
}