package com.aigovernance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PolicyUpsertRequest(
        @NotBlank String name,
        @NotBlank String dataType,
        String allowedTools,
        String prohibitedTools,
        boolean approvalRequired,
        String riskLevel,
        boolean exceptionAllowed,
        @Min(1) @Max(365) Integer maxDurationDays,
        boolean enabled,
        String description
) {}
