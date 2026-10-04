package com.aigovernance.intelligence;

public record IntentAnalysis(
        String normalizedIntent,
        String intentType,
        String inferredDataType,
        String inferredRisk,
        double confidence
) {}
