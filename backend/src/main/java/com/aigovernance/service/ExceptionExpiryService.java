package com.aigovernance.service;

import com.aigovernance.model.ExceptionRequest;
import com.aigovernance.model.ExceptionStatus;
import com.aigovernance.model.RequestEntity;
import com.aigovernance.repository.ExceptionRequestRepository;
import com.aigovernance.repository.RequestRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Automatically expires approved governance exceptions
 * after their configured expiry time.
 */
@Service
public class ExceptionExpiryService {

    private final ExceptionRequestRepository exceptions;
    private final RequestRepository requests;
    private final AuditService audit;

    public ExceptionExpiryService(
            ExceptionRequestRepository exceptions,
            RequestRepository requests,
            AuditService audit
    ) {
        this.exceptions = exceptions;
        this.requests = requests;
        this.audit = audit;
    }

    /**
     * Runs periodically and marks expired approved exceptions.
     *
     * The job intentionally runs every minute so that
     * expiry is automatic without requiring a user action.
     */
    @Scheduled(fixedRate = 60_000)
    public void expireApprovedExceptions() {

        Instant now = Instant.now();

        List<ExceptionRequest> expired =
                exceptions.findByStatusAndExpiresAtBefore(
                        ExceptionStatus.APPROVED,
                        now
                );

        for (ExceptionRequest exception : expired) {
            TenantContext.set(exception.tenantId);

            /*
             * Protect against an already-processed record.
             */
            if (exception.status != ExceptionStatus.APPROVED) {
                continue;
            }

            exception.status = ExceptionStatus.EXPIRED;
            exception.updatedAt = now;

            exceptions.save(exception);

            /*
             * Update the original governance request.
             *
             * An expired exception must not remain
             * permanently approved.
             */
            requests.findByTenantIdAndId(exception.tenantId, exception.requestId)
                    .ifPresent(request -> {

                        request.status = "EXPIRED";
                        request.decision = "EXCEPTION_EXPIRED";
                        request.decisionReason =
                                "The temporary governance exception expired on "
                                + exception.expiresAt;

                        request.updatedAt = now;

                        requests.save(request);

                        audit.record(
                                "REQUEST",
                                request.id,
                                "EXCEPTION_EXPIRED",
                                null,
                                "exception=" + exception.id
                        );
                    });

            audit.record(
                    "EXCEPTION",
                    exception.id,
                    "EXPIRED",
                    null,
                    "expiresAt=" + exception.expiresAt
            );
        }
        TenantContext.clear();
    }
}