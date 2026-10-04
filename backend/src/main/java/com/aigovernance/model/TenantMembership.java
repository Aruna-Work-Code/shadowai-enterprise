package com.aigovernance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tenant_membership",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_tenant_user",
               columnNames = {"tenant_id", "user_id"}))
public class TenantMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Role role;
}
