package com.aigovernance.controller;

import com.aigovernance.dto.RequestCreate;
import com.aigovernance.model.RequestEntity;
import com.aigovernance.model.User;
import com.aigovernance.repository.RequestRepository;
import com.aigovernance.repository.UserRepository;
import com.aigovernance.service.AuditService;
import com.aigovernance.service.RecommendationService;
import com.aigovernance.tenant.TenantContext;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
public class RequestController {

    private final RequestRepository repo;
    private final RecommendationService rec;
    private final AuditService audit;
    private final UserRepository users;

    public RequestController(
            RequestRepository repo,
            RecommendationService rec,
            AuditService audit,
            UserRepository users) {

        this.repo = repo;
        this.rec = rec;
        this.audit = audit;
        this.users = users;
    }

    /**
     * ============================================================
     * GOVERNANCE REQUEST QUEUE
     * ============================================================
     *
     * Employees MUST NOT be able to access this endpoint.
     *
     * Allowed:
     * - IT_REVIEWER
     * - SECURITY_ANALYST
     * - GOVERNANCE_MANAGER
     * - ADMIN
     *
     * Employee requests are still created through POST /api/v1/requests.
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')"
    )
    public List<RequestEntity> all(Authentication authentication) {

        long tenantId = TenantContext.currentOrDefault();

        /*
         * Return only requests belonging to the authenticated
         * user's current tenant.
         *
         * This preserves tenant isolation.
         */
        return repo.findByTenantId(tenantId);
    }

    /**
     * ============================================================
     * CREATE GOVERNANCE REQUEST
     * ============================================================
     *
     * Employees are allowed to create requests.
     *
     * The employeeId supplied by the frontend/request body is
     * intentionally ignored.
     *
     * The authenticated JWT identity determines the employee.
     *
     * The TenantContext determines the tenant.
     */
    @PostMapping
    @PreAuthorize(
            "hasAnyRole('EMPLOYEE','IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public RequestEntity create(
            @Valid @RequestBody RequestCreate request,
            Authentication authentication) {

        String username = authentication.getName();

        /*
         * Resolve the authenticated user from the JWT username.
         */
        User employee = users
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user does not exist"
                        )
                );

        /*
         * Run deterministic recommendation logic.
         */
        var recommendation = rec.recommend(
                request.intent(),
                request.dataType(),
                request.department(),
                request.frequency()
        );

        RequestEntity entity = new RequestEntity();

        /*
         * Never trust employeeId from the frontend.
         */
        entity.employeeId = employee.id;

        /*
         * Copy validated request data.
         */
        entity.intent = request.intent();
        entity.dataType = request.dataType();
        entity.department = request.department();
        entity.frequency = request.frequency();
        entity.requestedTool = request.requestedTool();

        /*
         * Tenant comes from authenticated tenant context.
         */
        entity.tenantId = TenantContext.currentOrDefault();

        entity.updatedAt = Instant.now();

        /*
         * Apply deterministic recommendation result.
         */
        if (Boolean.TRUE.equals(
                recommendation.get("match"))) {

            entity.status = "RECOMMENDED";

            entity.recommendedTool =
                    (String) recommendation.get("tool");

            entity.recommendationScore =
                    (Integer) recommendation.get("score");

            entity.confidence =
                    (String) recommendation.get("confidence");

        } else {

            /*
             * No safe recommendation means the request
             * requires governance exception review.
             */
            entity.status = "EXCEPTION_REQUIRED";
        }

        /*
         * Persist request.
         */
        entity = repo.save(entity);

        /*
         * Record immutable audit event.
         */
        audit.record(
                "REQUEST",
                entity.id,
                "CREATED",
                employee.id,
                "status=" + entity.status
        );

        return entity;
    }

    /**
     * ============================================================
     * APPROVE REQUEST
     * ============================================================
     *
     * Only governance/reviewer roles can approve.
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public RequestEntity approve(
            @PathVariable Long id,
            @RequestParam(
                    defaultValue = "Approved after review"
            ) String reason) {

        /*
         * Tenant-aware lookup prevents cross-tenant access.
         */
        RequestEntity entity =
                repo.findByTenantIdAndId(
                        TenantContext.currentOrDefault(),
                        id
                ).orElseThrow();

        entity.status = "APPROVED";
        entity.decision = "APPROVE";
        entity.decisionReason = reason;
        entity.updatedAt = Instant.now();

        entity = repo.save(entity);

        /*
         * Audit approval.
         */
        audit.record(
                "REQUEST",
                entity.id,
                "APPROVED",
                null,
                reason
        );

        return entity;
    }

    /**
     * ============================================================
     * REJECT REQUEST
     * ============================================================
     *
     * Only governance/reviewer roles can reject.
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public RequestEntity reject(
            @PathVariable Long id,
            @RequestParam String reason) {

        /*
         * Tenant-aware lookup prevents cross-tenant access.
         */
        RequestEntity entity =
                repo.findByTenantIdAndId(
                        TenantContext.currentOrDefault(),
                        id
                ).orElseThrow();

        entity.status = "REJECTED";
        entity.decision = "REJECT";
        entity.decisionReason = reason;
        entity.updatedAt = Instant.now();

        entity = repo.save(entity);

        /*
         * Audit rejection.
         */
        audit.record(
                "REQUEST",
                entity.id,
                "REJECTED",
                null,
                reason
        );

        return entity;
    }
}