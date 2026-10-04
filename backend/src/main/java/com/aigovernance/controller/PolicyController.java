package com.aigovernance.controller;

import com.aigovernance.dto.PolicyUpsertRequest;
import com.aigovernance.model.Policy;
import com.aigovernance.service.PolicyService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController {
    private final PolicyService service;

    public PolicyController(PolicyService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
    public List<Policy> all() {
        return service.all();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GOVERNANCE_MANAGER','ADMIN')")
    public Policy create(@Valid @RequestBody PolicyUpsertRequest r) {
        return service.create(r);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOVERNANCE_MANAGER','ADMIN')")
    public Policy update(@PathVariable Long id, @Valid @RequestBody PolicyUpsertRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOVERNANCE_MANAGER','ADMIN')")
    public void disable(@PathVariable Long id) {
        service.disable(id);
    }
}
