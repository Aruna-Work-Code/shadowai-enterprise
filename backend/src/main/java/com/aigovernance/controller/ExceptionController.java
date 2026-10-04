package com.aigovernance.controller;

import com.aigovernance.dto.ExceptionCreateRequest;
import com.aigovernance.dto.ExceptionDecisionRequest;
import com.aigovernance.model.ExceptionRequest;
import com.aigovernance.service.ExceptionService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Governance Resolution Intelligence.
 */
@RestController
@RequestMapping("/api/v1/exceptions")
public class ExceptionController {

    private final ExceptionService service;

    public ExceptionController(ExceptionService service) {
        this.service = service;
    }

    /**
     * List governance exceptions.
     *
     * Reviewers and administrators can access
     * the governance exception queue.
     */
    @GetMapping
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public List<ExceptionRequest> all() {
        return service.all();
    }

    /**
     * Employee creates an exception request.
     */
    @PostMapping
    @PreAuthorize(
            "hasAnyRole('EMPLOYEE','IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public ExceptionRequest create(
            @Valid @RequestBody ExceptionCreateRequest request
    ) {
        return service.create(request);
    }

    /**
     * Governance reviewer approves an exception.
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public ExceptionRequest approve(
            @PathVariable Long id,
            @Valid @RequestBody ExceptionDecisionRequest decision
    ) {
        return service.approve(id, decision);
    }

    /**
     * Governance reviewer rejects an exception.
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize(
            "hasAnyRole('IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public ExceptionRequest reject(
            @PathVariable Long id,
            @Valid @RequestBody ExceptionDecisionRequest decision
    ) {
        return service.reject(id, decision);
    }
}