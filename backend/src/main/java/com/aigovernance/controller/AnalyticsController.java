package com.aigovernance.controller;

import com.aigovernance.model.ExceptionStatus;
import com.aigovernance.repository.AuditRepository;
import com.aigovernance.repository.ExceptionRequestRepository;
import com.aigovernance.repository.IntegrationEventRepository;
import com.aigovernance.repository.InvestigationRepository;
import com.aigovernance.repository.RequestRepository;
import com.aigovernance.tenant.TenantContext;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final RequestRepository requests;
    private final InvestigationRepository investigations;
    private final ExceptionRequestRepository exceptions;
    private final IntegrationEventRepository events;
    private final AuditRepository audits;

    public AnalyticsController(
            RequestRepository requests,
            InvestigationRepository investigations,
            ExceptionRequestRepository exceptions,
            IntegrationEventRepository events,
            AuditRepository audits
    ) {
        this.requests = requests;
        this.investigations = investigations;
        this.exceptions = exceptions;
        this.events = events;
        this.audits = audits;
    }

    @GetMapping("/overview")
    @PreAuthorize(
            "hasAnyRole(" +
                    "'IT_REVIEWER'," +
                    "'SECURITY_ANALYST'," +
                    "'GOVERNANCE_MANAGER'," +
                    "'ADMIN'" +
                    ")"
    )
    public Map<String, Object> overview() {

        long tenantId =
                TenantContext.currentOrDefault();

        /*
         * ==============================
         * REQUEST METRICS
         * ==============================
         */

        long total =
                requests.countByTenantId(tenantId);

        long approved =
                requests
                        .findByTenantIdAndStatus(
                                tenantId,
                                "APPROVED"
                        )
                        .size();

        long rejected =
                requests
                        .findByTenantIdAndStatus(
                                tenantId,
                                "REJECTED"
                        )
                        .size();

        long exceptionRequired =
                requests
                        .findByTenantIdAndStatus(
                                tenantId,
                                "EXCEPTION_REQUIRED"
                        )
                        .size();

        long expired =
                requests
                        .findByTenantIdAndStatus(
                                tenantId,
                                "EXPIRED"
                        )
                        .size();

        /*
         * ==============================
         * INVESTIGATION METRICS
         * ==============================
         */

        long openInvestigations =
                investigations
                        .findByTenantIdAndStatus(
                                tenantId,
                                "OPEN"
                        )
                        .size();

        long resolvedInvestigations =
                investigations
                        .findByTenantIdAndStatus(
                                tenantId,
                                "RESOLVED"
                        )
                        .size();

        /*
         * ==============================
         * EXCEPTION METRICS
         * ==============================
         */

        long pendingExceptions =
                exceptions
                        .findByTenantIdAndStatus(
                                tenantId,
                                ExceptionStatus.PENDING_REVIEW
                        )
                        .size();

        long approvedExceptions =
                exceptions
                        .findByTenantIdAndStatus(
                                tenantId,
                                ExceptionStatus.APPROVED
                        )
                        .size();

        long rejectedExceptions =
                exceptions
                        .findByTenantIdAndStatus(
                                tenantId,
                                ExceptionStatus.REJECTED
                        )
                        .size();

        long expiredExceptions =
                exceptions
                        .findByTenantIdAndStatus(
                                tenantId,
                                ExceptionStatus.EXPIRED
                        )
                        .size();

        /*
         * ==============================
         * INTEGRATION METRICS
         * ==============================
         */

        long integrationEvents =
                events.countByTenantId(tenantId);

        long failedIntegrationEvents =
                events
                        .findByTenantIdAndProcessingStatus(
                                tenantId,
                                "FAILED"
                        )
                        .size();

        /*
         * ==============================
         * AUDIT METRICS
         * ==============================
         */

        long auditEvents =
                audits.countByTenantId(tenantId);

        /*
         * ==============================
         * APPROVAL RATE
         * ==============================
         */

        double approvalRate =
                total == 0
                        ? 0.0
                        : Math.round(
                                approved * 10000.0 / total
                        ) / 100.0;

        /*
         * ==============================
         * RESPONSE
         * ==============================
         *
         * LinkedHashMap is intentionally used
         * instead of Map.of().
         *
         * Map.of() supports only up to
         * ten key/value pairs.
         */

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "tenantId",
                tenantId
        );

        result.put(
                "totalRequests",
                total
        );

        result.put(
                "approved",
                approved
        );

        result.put(
                "rejected",
                rejected
        );

        result.put(
                "exceptionRequired",
                exceptionRequired
        );

        result.put(
                "expired",
                expired
        );

        result.put(
                "openInvestigations",
                openInvestigations
        );

        result.put(
                "resolvedInvestigations",
                resolvedInvestigations
        );

        result.put(
                "pendingExceptions",
                pendingExceptions
        );

        result.put(
                "approvedExceptions",
                approvedExceptions
        );

        result.put(
                "rejectedExceptions",
                rejectedExceptions
        );

        result.put(
                "expiredExceptions",
                expiredExceptions
        );

        result.put(
                "integrationEvents",
                integrationEvents
        );

        result.put(
                "failedIntegrationEvents",
                failedIntegrationEvents
        );

        result.put(
                "auditEvents",
                auditEvents
        );

        result.put(
                "approvalRate",
                approvalRate
        );

        return result;
    }
}