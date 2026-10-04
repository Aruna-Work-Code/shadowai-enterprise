package com.aigovernance.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="refresh_token", indexes={@Index(name="idx_refresh_token_hash", columnList="token_hash")})
public class RefreshToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @Column(name="user_id", nullable=false) public Long userId;
    @Column(name="tenant_id", nullable=false) public Long tenantId;
    @Column(name="token_hash", nullable=false, unique=true, length=64) public String tokenHash;
    @Column(name="expires_at", nullable=false) public Instant expiresAt;
    @Column(nullable=false) public boolean revoked=false;
    @Column(name="created_at", nullable=false) public Instant createdAt=Instant.now();
    @Column(name="rotated_from_id") public Long rotatedFromId;
}
