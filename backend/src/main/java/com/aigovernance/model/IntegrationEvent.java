package com.aigovernance.model;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@EntityListeners(TenantEntityListener.class) @Table(name="integration_event",
 uniqueConstraints=@UniqueConstraint(name="uk_provider_event",columnNames={"provider","provider_event_id"}))
public class IntegrationEvent {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 public String provider;
 public String providerEventId;
 public String eventType;
 @Column(columnDefinition="TEXT") public String payload;
 public String processingStatus;
 public String errorMessage;
 public Instant receivedAt=Instant.now();
 public Instant processedAt;
}
