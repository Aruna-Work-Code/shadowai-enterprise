package com.aigovernance.controller;

import com.aigovernance.model.AiTool;
import com.aigovernance.repository.AiToolRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Governed AI tool inventory used by the AI Tools console.
 * Discovery writes to the same tenant-scoped inventory.
 */
@RestController
@RequestMapping("/api/v1/tools")
@PreAuthorize("hasAnyRole('IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
public class AiToolController {

    private final AiToolRepository repository;

    public AiToolController(AiToolRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AiTool> all() {
        return repository.findByTenantId(TenantContext.currentOrDefault());
    }
}
