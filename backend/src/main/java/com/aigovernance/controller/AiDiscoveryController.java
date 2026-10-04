package com.aigovernance.controller;

import com.aigovernance.discovery.AiDiscoveryService;
import com.aigovernance.model.AiTool;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shadow AI discovery API.
 *
 * This is intentionally additive to the existing AI-tool catalogue and
 * integration/webhook workflow. Existing endpoints remain unchanged.
 */
@RestController
@RequestMapping("/api/v1/discovery")
public class AiDiscoveryController {

    private final AiDiscoveryService discovery;

    public AiDiscoveryController(AiDiscoveryService discovery) {
        this.discovery = discovery;
    }

    @GetMapping("/tools")
    @PreAuthorize("hasAnyRole('IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
    public List<AiTool> inventory() {
        return discovery.inventory();
    }

    @PostMapping("/tools")
    @PreAuthorize("hasAnyRole('IT_REVIEWER','SECURITY_ANALYST','GOVERNANCE_MANAGER','ADMIN')")
    public AiDiscoveryService.DiscoveryResult discover(
            @Valid @RequestBody AiDiscoveryService.DiscoveryRequest request) {
        return discovery.discover(request);
    }
}
