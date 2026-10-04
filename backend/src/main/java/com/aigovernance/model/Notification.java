package com.aigovernance.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@EntityListeners(TenantEntityListener.class)
@Table(name="notification")
public class Notification {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    public Long userId;
    public String type;
    public String title;
    @Column(columnDefinition="TEXT") public String message;
    public boolean readFlag=false;
    public Instant createdAt=Instant.now();
}
