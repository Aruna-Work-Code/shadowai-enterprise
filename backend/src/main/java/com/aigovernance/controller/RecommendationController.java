package com.aigovernance.controller;

import com.aigovernance.service.RecommendationService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(
            RecommendationService service
    ) {
        this.service = service;
    }

    /**
     * Generate a governance-aware recommendation.
     */
    @PostMapping
    @PreAuthorize(
            "hasAnyRole('EMPLOYEE','IT_REVIEWER','GOVERNANCE_MANAGER','ADMIN')"
    )
    public Map<String, Object> recommend(

            @RequestParam
            @NotBlank
            String intent,

            @RequestParam
            @NotBlank
            String dataType,

            @RequestParam
            @NotBlank
            String department,

            @RequestParam
            @NotBlank
            String frequency
    ) {

        return service.recommend(
                intent,
                dataType,
                department,
                frequency
        );
    }
}