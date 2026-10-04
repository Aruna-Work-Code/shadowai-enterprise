package com.aigovernance.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestCreate(
        @NotBlank String intent,
        @NotBlank String dataType,
        @NotBlank String department,
        @NotBlank String frequency,
        String requestedTool
) {
}