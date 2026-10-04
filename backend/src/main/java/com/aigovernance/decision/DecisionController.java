package com.aigovernance.controller;

import com.aigovernance.decision.DecisionEngine;
import com.aigovernance.decision.DecisionResult;
import com.aigovernance.model.RequestEntity;
import com.aigovernance.repository.AiToolRepository;
import com.aigovernance.repository.PolicyRepository;
import com.aigovernance.repository.RequestRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import com.aigovernance.tenant.TenantContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/decisions")
public class DecisionController {

    private final RequestRepository requests;
    private final AiToolRepository tools;
    private final PolicyRepository policies;
    private final DecisionEngine engine;

    public DecisionController(
            RequestRepository requests,
            AiToolRepository tools,
            PolicyRepository policies,
            DecisionEngine engine
    ) {
        this.requests = requests;
        this.tools = tools;
        this.policies = policies;
        this.engine = engine;
    }

    @PostMapping("/{id}/evaluate")
    @PreAuthorize(
            "hasAnyRole('EMPLOYEE','IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public DecisionResult evaluate(
            @PathVariable Long id
    ) {

        RequestEntity request =
                requests.findByTenantIdAndId(TenantContext.currentOrDefault(),id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Request not found"
                                )
                        );

        return engine.evaluate(
                request,
                tools.findByTenantId(TenantContext.currentOrDefault()),
                policies.findByTenantId(TenantContext.currentOrDefault())
        );
    }
}