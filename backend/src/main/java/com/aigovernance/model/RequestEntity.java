package com.aigovernance.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@EntityListeners(TenantEntityListener.class) @Table(name="request")
public class RequestEntity {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public Long employeeId;
 @Column(columnDefinition="TEXT",nullable=false) public String intent;
 public String dataType;
 public String department;
 public String frequency;
 public String requestedTool;
 public String recommendedTool;
 public Integer recommendationScore;
 public String confidence;
 public String status;
 public String decision;
 @Column(columnDefinition="TEXT") public String decisionReason;
 public Instant createdAt=Instant.now();
 public Instant updatedAt=Instant.now();
}
