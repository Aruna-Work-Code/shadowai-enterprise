package com.aigovernance.decision;

import java.util.List;

public record DecisionResult(
        String decision,
        String recommendedTool,
        int score,
        String confidence,
        String riskLevel,
        List<String> reasons,
        List<String> constraints
) {
}