package com.aigovernance.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@EntityListeners(TenantEntityListener.class)
@Table(name = "integration_connection")
public class IntegrationConnection {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String provider;

    @Column(nullable = false)
    public boolean enabled = true;

    /*
     * Never expose the raw integration secret.
     *
     * This field stores only a BCrypt hash.
     */
    @Column(name = "webhook_secret_hash")
    public String webhookSecretHash;

    public Long createdBy;

    public Instant createdAt = Instant.now();
}