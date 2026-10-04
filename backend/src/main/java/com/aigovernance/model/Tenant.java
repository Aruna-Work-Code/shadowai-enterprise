package com.aigovernance.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tenant")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 120)
    public String name;

    @Column(nullable = false, unique = true, length = 80)
    public String key;

    @Column(nullable = false)
    public boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt = Instant.now();
}
