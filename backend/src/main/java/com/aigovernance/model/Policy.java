package com.aigovernance.model;
import jakarta.persistence.*;

@Entity
@EntityListeners(TenantEntityListener.class) @Table(name="policy")
public class Policy {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 @Column(nullable=false) public String dataType;
 public String allowedTools;
 public String prohibitedTools;
 public boolean approvalRequired;
 public String riskLevel;
 public boolean exceptionAllowed=false;
 public Integer maxDurationDays;
 public boolean enabled=true;
 @Column(columnDefinition="TEXT") public String description;
}
