package com.aigovernance.model;
import jakarta.persistence.*;

@Entity
@EntityListeners(TenantEntityListener.class) @Table(name="ai_tool")
public class AiTool {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false) public String name;
 public String category;
 @Column(columnDefinition="TEXT") public String capabilities;
 public String approvalStatus;
 public String riskLevel;
 public String dataPolicy;
 public String owner;
}
