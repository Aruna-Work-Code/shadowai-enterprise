package com.aigovernance.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@EntityListeners(TenantEntityListener.class) @Table(name="audit_event")
public class AuditEvent {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String entityType;
 public Long entityId;
 public String action;
 public Long actorId;
 @Column(columnDefinition="TEXT") public String metadata;
 public Instant createdAt=Instant.now();
 public String previousHash;
 public String eventHash;
}
