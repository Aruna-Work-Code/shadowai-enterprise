package com.aigovernance.service;

import com.aigovernance.dto.ExceptionCreateRequest;
import com.aigovernance.dto.ExceptionDecisionRequest;
import com.aigovernance.model.ExceptionRequest;
import com.aigovernance.model.ExceptionStatus;
import com.aigovernance.model.RequestEntity;
import com.aigovernance.repository.ExceptionRequestRepository;
import com.aigovernance.repository.RequestRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Governance Resolution Intelligence service.
 *
 * Responsible for:
 *
 * 1. Creating governance exceptions.
 * 2. Preventing duplicate active/pending exceptions.
 * 3. Approving exceptions.
 * 4. Rejecting exceptions.
 * 5. Setting an actual expiry date for approved exceptions.
 * 6. Updating the original governance request.
 * 7. Recording governance audit events.
 *
 * Important governance principle:
 *
 * An exception does NOT make the underlying AI tool generally approved.
 * It only creates a controlled, time-bound approval for the specific
 * governance request.
 */
@Service
public class ExceptionService {

    private final ExceptionRequestRepository exceptions;
    private final RequestRepository requests;
    private final AuditService audit;

    public ExceptionService(
            ExceptionRequestRepository exceptions,
            RequestRepository requests,
            AuditService audit
    ) {
        this.exceptions = exceptions;
        this.requests = requests;
        this.audit = audit;
    }

    /**
     * Creates a governance exception request.
     *
     * An exception can only be created when the recommendation engine
     * determined that no approved capability could safely satisfy the
     * request.
     */
    public ExceptionRequest create(ExceptionCreateRequest input) {

        RequestEntity request = requests.findByTenantIdAndId(TenantContext.currentOrDefault(), input.requestId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Governance request not found"
                        )
                );

        /*
         * Only requests that failed the normal policy/recommendation
         * workflow can enter the exception process.
         */
        if (!"EXCEPTION_REQUIRED".equalsIgnoreCase(request.status)) {
            throw new IllegalArgumentException(
                    "An exception can only be created for an EXCEPTION_REQUIRED request"
            );
        }

        /*
         * Prevent duplicate pending exception requests for the
         * same governance request.
         */
        if (exceptions.existsByTenantIdAndRequestIdAndStatus(TenantContext.currentOrDefault(), 
                request.id,
                ExceptionStatus.PENDING_REVIEW
        )) {
            throw new IllegalArgumentException(
                    "A pending exception already exists for this request"
            );
        }

        ExceptionRequest exception = new ExceptionRequest();

        exception.requestId = request.id;
        exception.requestedBy = input.requestedBy();
        exception.businessJustification =
                input.businessJustification();

        exception.requestedDurationDays =
                input.requestedDurationDays();

        exception.status =
                ExceptionStatus.PENDING_REVIEW;

        exception.createdAt = Instant.now();
        exception.updatedAt = Instant.now();

        exception = exceptions.save(exception);

        /*
         * Every exception creation is recorded in the audit trail.
         */
        audit.record(
                "EXCEPTION",
                exception.id,
                "CREATED",
                input.requestedBy(),
                "request=" + request.id
        );

        return exception;
    }

    /**
     * Returns all governance exception records.
     */
    public List<ExceptionRequest> all() {
        return exceptions.findByTenantId(TenantContext.currentOrDefault());
    }

    /**
     * Approves a pending governance exception.
     *
     * IMPORTANT:
     *
     * Approval is time-bound.
     *
     * Example:
     *
     * requestedDurationDays = 7
     *
     * approval time = 2026-08-20
     *
     * expiresAt = 2026-08-27
     *
     * This prevents an exception from silently becoming permanent.
     */
    public ExceptionRequest approve(
            Long id,
            ExceptionDecisionRequest decision
    ) {

        ExceptionRequest exception =
                exceptions.findByTenantIdAndId(TenantContext.currentOrDefault(), id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Exception not found"
                                )
                        );

        /*
         * An exception can only be approved once.
         */
        ensurePending(exception);

        Instant now = Instant.now();

        /*
         * Record the governance decision.
         */
        exception.status = ExceptionStatus.APPROVED;
        exception.reviewerId = decision.reviewerId();
        exception.decisionReason = decision.reason();
        exception.decidedAt = now;

        /*
         * Calculate the real expiry date.
         *
         * The requested duration comes from the exception request.
         *
         * Example:
         * 7 days -> now + 7 days
         */
        if (exception.requestedDurationDays == null
                || exception.requestedDurationDays <= 0) {

            throw new IllegalArgumentException(
                    "Exception duration must be greater than zero days"
            );
        }

        exception.expiresAt = now.plusSeconds(
                exception.requestedDurationDays * 24L * 60L * 60L
        );

        exception.updatedAt = now;

        exception = exceptions.save(exception);

        /*
         * The original governance request is now approved
         * specifically through the human exception process.
         */
        RequestEntity request =
                requests.findByTenantIdAndId(TenantContext.currentOrDefault(), exception.requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Original request not found"
                                )
                        );

        request.status = "APPROVED";

        /*
         * This is intentionally different from a normal approval.
         *
         * EXCEPTION_APPROVED means:
         *
         * "This request was approved through an exception workflow."
         */
        request.decision = "EXCEPTION_APPROVED";

        request.decisionReason = decision.reason();
        request.updatedAt = now;

        requests.save(request);

        /*
         * Audit the exception approval.
         */
        audit.record(
                "EXCEPTION",
                exception.id,
                "APPROVED",
                decision.reviewerId(),
                "reason=" + decision.reason()
                        + ";expiresAt=" + exception.expiresAt
        );

        /*
         * Audit the corresponding request decision.
         */
        audit.record(
                "REQUEST",
                request.id,
                "EXCEPTION_APPROVED",
                decision.reviewerId(),
                "exception=" + exception.id
                        + ";expiresAt=" + exception.expiresAt
        );

        return exception;
    }

    /**
     * Rejects a pending governance exception.
     *
     * The original governance request is also rejected because
     * no normal approved capability was available and the requested
     * exception was not granted.
     */
    public ExceptionRequest reject(
            Long id,
            ExceptionDecisionRequest decision
    ) {

        ExceptionRequest exception =
                exceptions.findByTenantIdAndId(TenantContext.currentOrDefault(), id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Exception not found"
                                )
                        );

        /*
         * Prevent a previously decided exception from being
         * approved/rejected again.
         */
        ensurePending(exception);

        Instant now = Instant.now();

        exception.status = ExceptionStatus.REJECTED;
        exception.reviewerId = decision.reviewerId();
        exception.decisionReason = decision.reason();
        exception.decidedAt = now;

        /*
         * Rejected exceptions never receive an expiry date.
         */
        exception.expiresAt = null;

        exception.updatedAt = now;

        exception = exceptions.save(exception);

        /*
         * Find the original governance request.
         */
        RequestEntity request =
                requests.findByTenantIdAndId(TenantContext.currentOrDefault(), exception.requestId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Original request not found"
                                )
                        );

        request.status = "REJECTED";
        request.decision = "EXCEPTION_REJECTED";
        request.decisionReason = decision.reason();
        request.updatedAt = now;

        requests.save(request);

        /*
         * Audit the exception rejection.
         */
        audit.record(
                "EXCEPTION",
                exception.id,
                "REJECTED",
                decision.reviewerId(),
                decision.reason()
        );

        /*
         * Audit the corresponding request decision.
         */
        audit.record(
                "REQUEST",
                request.id,
                "EXCEPTION_REJECTED",
                decision.reviewerId(),
                "exception=" + exception.id
        );

        return exception;
    }

    /**
     * Ensures that the exception is still awaiting governance review.
     *
     * This protects against:
     *
     * - double approval
     * - approval after rejection
     * - rejection after approval
     * - repeated governance decisions
     */
    private void ensurePending(ExceptionRequest exception) {

        if (exception.status != ExceptionStatus.PENDING_REVIEW) {
            throw new IllegalStateException(
                    "Exception has already been decided"
            );
        }
    }
}