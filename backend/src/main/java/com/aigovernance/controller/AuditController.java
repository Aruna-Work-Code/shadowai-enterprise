package com.aigovernance.controller;

import com.aigovernance.repository.AuditRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Governance audit history. */
@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasAnyRole('IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
public class AuditController {

    private final AuditRepository repository;

    public AuditController(AuditRepository repository) {
        this.repository = repository;
    }

    /** Latest tenant audit events for the governance console. */
    @GetMapping
    public List<?> latest(@RequestParam(defaultValue = "100") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 250));
        return repository.findByTenantIdOrderByCreatedAtDesc(
                        TenantContext.currentOrDefault())
                .stream()
                .limit(safeLimit)
                .toList();
    }

    @GetMapping("/{type}/{id}")
    public Object history(
            @PathVariable String type,
            @PathVariable Long id) {
        return repository.findByTenantIdAndEntityTypeAndEntityId(
                TenantContext.currentOrDefault(), type, id);
    }
}
