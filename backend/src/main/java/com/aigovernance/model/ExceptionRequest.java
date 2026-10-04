package com.aigovernance.model;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Represents a temporary governance exception requested
 * for a request that could not receive an automatically
 * approved AI capability.
 */
@Entity
@EntityListeners(TenantEntityListener.class)
@Table(name = "exception_request")
public class ExceptionRequest {
    @Column(name = "tenant_id", nullable = false)
    public Long tenantId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Original governance request.
     */
    @Column(name = "request_id", nullable = false)
    public Long requestId;

    /**
     * Employee/user who submitted the exception.
     */
    @Column(name = "requested_by", nullable = false)
    public Long requestedBy;

    /**
     * Why the employee believes the exception is required.
     */
    @Column(name = "business_justification",
            columnDefinition = "TEXT",
            nullable = false)
    public String businessJustification;

    /**
     * Number of days for which the exception is requested.
     */
    @Column(name = "requested_duration_days",
            nullable = false)
    public Integer requestedDurationDays;

    /**
     * Current governance decision state.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ExceptionStatus status = ExceptionStatus.PENDING_REVIEW;

    /**
     * Reviewer who made the decision.
     */
    @Column(name = "reviewer_id")
    public Long reviewerId;

    /**
     * Explanation for approval/rejection.
     */
    @Column(name = "decision_reason",
            columnDefinition = "TEXT")
    public String decisionReason;

    @Column(name = "created_at",
            nullable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "updated_at",
            nullable = false)
    public Instant updatedAt = Instant.now();

    @Column(name = "decided_at")
    public Instant decidedAt;

    @Column(name = "expires_at")
public Instant expiresAt;
}