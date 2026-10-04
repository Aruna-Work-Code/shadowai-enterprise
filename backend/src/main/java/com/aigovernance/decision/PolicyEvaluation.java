package com.aigovernance.decision;

import java.util.List;

public record PolicyEvaluation(
        boolean allowed,
        String riskLevel,
        List<String> constraints,
        List<String> reasons
) {
}