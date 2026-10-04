package com.aigovernance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Payload used by a governance reviewer to decide
 * whether an exception should be approved or rejected.
 */
public record ExceptionDecisionRequest(

        @NotNull
        Long reviewerId,

        @NotBlank
        String reason
) {
}