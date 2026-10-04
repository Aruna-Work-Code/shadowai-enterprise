package com.aigovernance.controller;

import com.aigovernance.model.Role;
import com.aigovernance.model.Tenant;
import com.aigovernance.model.TenantMembership;
import com.aigovernance.service.TenantService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tenants")
@PreAuthorize("hasRole('ADMIN')")
public class TenantController {

    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @GetMapping
    public List<Tenant> all() {
        return service.all();
    }

    @PostMapping
    public Tenant create(
            @RequestParam @NotBlank String name,
            @RequestParam @NotBlank String key) {
        return service.create(name, key);
    }

    @GetMapping("/{id}/members")
    public List<TenantMembership> members(@PathVariable Long id) {
        return service.members(id);
    }

    @PostMapping("/{tenantId}/members")
    public TenantMembership addMember(
            @PathVariable Long tenantId,
            @RequestParam Long userId,
            @RequestParam Role role) {
        return service.addMember(tenantId, userId, role);
    }
}
