package com.aigovernance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload used when an employee requests a governance exception.
 */
public record ExceptionCreateRequest(

        @NotNull
        Long requestId,

        @NotNull
        Long requestedBy,

        @NotBlank
        String businessJustification,

        @NotNull
        @Min(1)
        @Max(365)
        Integer requestedDurationDays
) {
}