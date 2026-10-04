package com.aigovernance.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@EntityListeners(TenantEntityListener.class) @Table(name="investigation")
public class Investigation {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String title;
 public String toolName;
 public String department;
 public String riskLevel;
 public Long ownerId;
 public String status;
 @Column(columnDefinition="TEXT") public String businessContext;
 @Column(columnDefinition="TEXT") public String rootCause;
 @Column(columnDefinition="TEXT") public String recommendedAction;
 @Column(columnDefinition="TEXT") public String resolution;
 public Instant createdAt=Instant.now();
 public Instant resolvedAt;
}
