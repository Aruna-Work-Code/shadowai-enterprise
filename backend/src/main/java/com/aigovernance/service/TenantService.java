package com.aigovernance.service;

import com.aigovernance.model.Role;
import com.aigovernance.model.Tenant;
import com.aigovernance.model.TenantMembership;
import com.aigovernance.repository.TenantMembershipRepository;
import com.aigovernance.repository.TenantRepository;
import com.aigovernance.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenants;
    private final TenantMembershipRepository memberships;
    private final UserRepository users;

    public TenantService(
            TenantRepository tenants,
            TenantMembershipRepository memberships,
            UserRepository users) {
        this.tenants = tenants;
        this.memberships = memberships;
        this.users = users;
    }

    public List<Tenant> all() {
        return tenants.findAll();
    }

    public Tenant create(String name, String key) {
        if (tenants.existsByKey(key)) {
            throw new IllegalArgumentException("Tenant key already exists");
        }
        Tenant tenant = new Tenant();
        tenant.name = name;
        tenant.key = key;
        return tenants.save(tenant);
    }

    public TenantMembership addMember(
            Long tenantId,
            Long userId,
            Role role) {

        tenants.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        TenantMembership membership = new TenantMembership();
        membership.tenantId = tenantId;
        membership.userId = userId;
        membership.role = role;
        return memberships.save(membership);
    }

    public List<TenantMembership> members(Long tenantId) {
        return memberships.findByTenantId(tenantId);
    }
}
