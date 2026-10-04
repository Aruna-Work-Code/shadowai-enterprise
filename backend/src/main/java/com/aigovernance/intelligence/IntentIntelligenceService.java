package com.aigovernance.intelligence;

import org.springframework.stereotype.Service;

/**
 * Deterministic intent intelligence.
 *
 * This is the safe local implementation. It intentionally does not make
 * governance decisions. A future LLM adapter may implement the same contract
 * for richer language understanding while the policy engine remains the
 * final authority.
 */
@Service
public class IntentIntelligenceService {
    public IntentAnalysis analyze(String text, String declaredDataType) {
        String t = text == null ? "" : text.toLowerCase();
        String type = "GENERAL";
        if (t.contains("debug") || t.contains("java") || t.contains("api") || t.contains("unit test"))
            type = "SOFTWARE_DEVELOPMENT";
        else if (t.contains("summar") || t.contains("research") || t.contains("complaint"))
            type = "RESEARCH_SUMMARIZATION";
        else if (t.contains("excel") || t.contains("spreadsheet") || t.contains("trend") || t.contains("analysis"))
            type = "DATA_ANALYSIS";

        String data = declaredDataType == null || declaredDataType.isBlank()
                ? (t.contains("customer") ? "CUSTOMER_DATA" : "INTERNAL")
                : declaredDataType.toUpperCase();

        String risk = "LOW";
        if ("CUSTOMER_DATA".equals(data) || t.contains("password") || t.contains("credential"))
            risk = "HIGH";
        else if ("CONFIDENTIAL".equals(data))
            risk = "MEDIUM";

        return new IntentAnalysis(text == null ? "" : text.trim(), type, data, risk,
                type.equals("GENERAL") ? 0.55 : 0.90);
    }
}
