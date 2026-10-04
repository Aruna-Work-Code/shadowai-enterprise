package com.aigovernance.decision;

public record ToolCandidate(
        Long toolId,
        String toolName,
        int score,
        String riskLevel,
        String reason
) {
}