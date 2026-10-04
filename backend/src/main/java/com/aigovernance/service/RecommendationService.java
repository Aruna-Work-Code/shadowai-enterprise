package com.aigovernance.service;

import com.aigovernance.model.AiTool;
import com.aigovernance.repository.AiToolRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Governance Recommendation Engine.
 *
 * Product principle:
 *
 *      INTENT
 *         ↓
 *      CAPABILITY CLASSIFICATION
 *         ↓
 *      APPROVAL HARD GATE
 *         ↓
 *      POLICY HARD GATE
 *         ↓
 *      CAPABILITY MATCH
 *         ↓
 *      RISK-AWARE SCORING
 *         ↓
 *      EXPLAINABLE RECOMMENDATION
 *
 * IMPORTANT:
 *
 * A tool must NEVER become eligible merely because it has a high score.
 *
 * Approval and data-policy checks happen BEFORE ranking.
 *
 * This prevents unsafe tools from winning because of capability
 * similarity.
 */
@Service
public class RecommendationService {

    private final AiToolRepository tools;

    public RecommendationService(AiToolRepository tools) {
        this.tools = tools;
    }

    /**
     * Main recommendation entry point.
     */
    public Map<String, Object> recommend(
            String intent,
            String dataType,
            String department,
            String frequency
    ) {

        /*
         * ------------------------------------------------------------
         * 1. Normalize input
         * ------------------------------------------------------------
         */

        String normalizedIntent =
                normalize(intent);

        String normalizedDataType =
                normalize(dataType);

        /*
         * ------------------------------------------------------------
         * 2. Classify the requested capability
         * ------------------------------------------------------------
         */

        String requestedCapability =
                classifyCapability(normalizedIntent);

        /*
         * ------------------------------------------------------------
         * 3. Reject unknown/nonsense intent
         * ------------------------------------------------------------
         *
         * We deliberately do NOT fall back to a general-purpose AI
         * assistant.
         *
         * This prevents:
         *
         *     "asdfgh qwerty xyz"
         *
         * from receiving an arbitrary recommendation.
         */

        if ("UNKNOWN".equals(requestedCapability)) {

            return noMatch(
                    "The requested task could not be confidently classified.",
                    List.of(
                            "No confident capability match was detected",
                            "The intent requires clarification before tool selection",
                            "Human/user clarification is required"
                    )
            );
        }

        /*
         * ------------------------------------------------------------
         * 4. Load all tools
         * ------------------------------------------------------------
         */

        List<AiTool> allTools =
                tools.findByTenantId(TenantContext.currentOrDefault());

        /*
         * ------------------------------------------------------------
         * 5. Approval HARD GATE
         * ------------------------------------------------------------
         *
         * UNAPPROVED tools can never be recommended.
         */

        List<AiTool> approvedTools =
                allTools.stream()
                        .filter(this::isApproved)
                        .toList();

        /*
         * ------------------------------------------------------------
         * 6. Policy HARD GATE
         * ------------------------------------------------------------
         *
         * Only approved tools whose data policy allows the requested
         * data type remain eligible.
         */

        List<AiTool> policyEligible =
                approvedTools.stream()
                        .filter(tool ->
                                policyAllows(
                                        tool,
                                        normalizedDataType
                                )
                        )
                        .toList();

        /*
         * ------------------------------------------------------------
         * 7. Capability matching
         * ------------------------------------------------------------
         */

        List<AiTool> capabilityMatches =
                policyEligible.stream()
                        .filter(tool ->
                                capabilityMatches(
                                        tool,
                                        requestedCapability
                                )
                        )
                        .toList();

        /*
         * ------------------------------------------------------------
         * 8. No capability match
         * ------------------------------------------------------------
         */

        if (capabilityMatches.isEmpty()) {

            return noMatch(
                    "No approved capability matched the requested task.",
                    List.of(
                            "Approved tools were checked",
                            "Policy hard-check passed for eligible tools",
                            "No eligible tool provides the requested capability",
                            "Human governance review is required"
                    )
            );
        }

        /*
         * ------------------------------------------------------------
         * 9. Rank matching tools
         * ------------------------------------------------------------
         */

        AiTool best =
                capabilityMatches.stream()
                        .max(
                                Comparator.comparingInt(
                                        tool -> score(
                                                tool,
                                                requestedCapability,
                                                normalizedDataType
                                        )
                                )
                        )
                        .orElseThrow();

        /*
         * ------------------------------------------------------------
         * 10. Calculate final score
         * ------------------------------------------------------------
         */

        int finalScore =
                score(
                        best,
                        requestedCapability,
                        normalizedDataType
                );

        String confidence =
                confidence(finalScore);

        /*
         * ------------------------------------------------------------
         * 11. Explain recommendation
         * ------------------------------------------------------------
         */

        List<String> reasons =
                buildReasons(
                        best,
                        requestedCapability
                );

        /*
         * ------------------------------------------------------------
         * 12. Return explainable result
         * ------------------------------------------------------------
         */

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("match", true);

        result.put(
                "tool",
                best.name
        );

        result.put(
                "score",
                finalScore
        );

        result.put(
                "confidence",
                confidence
        );

        result.put(
                "dataType",
                dataType
        );

        result.put(
                "department",
                department
        );

        result.put(
                "frequency",
                frequency
        );

        result.put(
                "requestedCapability",
                requestedCapability
        );

        result.put(
                "reasons",
                reasons
        );

        return result;
    }

    /*
     * ================================================================
     * CAPABILITY CLASSIFICATION
     * ================================================================
     */

    /**
     * Converts natural-language user intent into a controlled
     * capability category.
     */
    private String classifyCapability(
            String intent
    ) {

        /*
         * ------------------------------------------------------------
         * CODING
         * ------------------------------------------------------------
         */

        if (containsAny(
                intent,
                Set.of(
                        "code",
                        "coding",
                        "programming",
                        "debug",
                        "debugging",
                        "java",
                        "javascript",
                        "python",
                        "api",
                        "rest api",
                        "unit test",
                        "unit tests",
                        "testing",
                        "software development",
                        "developer"
                )
        )) {

            return "CODING";
        }

        /*
         * ------------------------------------------------------------
         * DATA ANALYSIS
         * ------------------------------------------------------------
         */

        if (containsAny(
                intent,
                Set.of(
                        "excel",
                        "spreadsheet",
                        "dataset",
                        "data analysis",
                        "data analytics",
                        "analyze data",
                        "analyse data",
                        "analyzing data",
                        "analysing data",
                        "trends",
                        "trend analysis",
                        "analytics",
                        "metrics",
                        "kpi",
                        "identify trends"
                )
        )) {

            return "DATA_ANALYSIS";
        }

        /*
         * ------------------------------------------------------------
         * SUMMARIZATION
         * ------------------------------------------------------------
         */

        if (containsAny(
                intent,
                Set.of(
                        "summarize",
                        "summarise",
                        "summary",
                        "summarization",
                        "summarisation",
                        "shorten document",
                        "condense document",
                        "key points"
                )
        )) {

            return "SUMMARIZATION";
        }

        /*
         * ------------------------------------------------------------
         * RESEARCH
         * ------------------------------------------------------------
         */

        if (containsAny(
                intent,
                Set.of(
                        "research",
                        "investigate",
                        "investigation",
                        "find sources",
                        "research sources",
                        "literature review",
                        "compare sources"
                )
        )) {

            return "RESEARCH";
        }

        /*
         * ------------------------------------------------------------
         * WRITING
         * ------------------------------------------------------------
         */

        if (containsAny(
                intent,
                Set.of(
                        "write",
                        "writing",
                        "draft",
                        "email",
                        "document",
                        "proposal",
                        "content",
                        "create a document"
                )
        )) {

            return "WRITING";
        }

        /*
         * ------------------------------------------------------------
         * UNKNOWN
         * ------------------------------------------------------------
         */

        return "UNKNOWN";
    }

    /*
     * ================================================================
     * CAPABILITY MATCHING
     * ================================================================
     */

    private boolean capabilityMatches(
            AiTool tool,
            String requestedCapability
    ) {

        String capabilities =
                normalize(
                        Optional.ofNullable(
                                tool.capabilities
                        ).orElse("")
                );

        return switch (requestedCapability) {

            case "CODING" ->
                    containsAny(
                            capabilities,
                            Set.of(
                                    "code",
                                    "coding",
                                    "debugging",
                                    "testing",
                                    "java",
                                    "api",
                                    "rest",
                                    "unit tests"
                            )
                    );

            case "DATA_ANALYSIS" ->
                    containsAny(
                            capabilities,
                            Set.of(
                                    "data",
                                    "analysis",
                                    "analytics",
                                    "excel",
                                    "spreadsheet",
                                    "trends"
                            )
                    );

            case "SUMMARIZATION" ->
                    containsAny(
                            capabilities,
                            Set.of(
                                    "summarization",
                                    "summarisation",
                                    "summary"
                            )
                    );

            case "RESEARCH" ->
                    containsAny(
                            capabilities,
                            Set.of(
                                    "research",
                                    "sources",
                                    "analysis"
                            )
                    );

            case "WRITING" ->
                    containsAny(
                            capabilities,
                            Set.of(
                                    "writing",
                                    "write",
                                    "document",
                                    "email"
                            )
                    );

            default ->
                    false;
        };
    }

    /*
     * ================================================================
     * APPROVAL HARD GATE
     * ================================================================
     */

    private boolean isApproved(
            AiTool tool
    ) {

        return tool.approvalStatus != null
                && "APPROVED".equalsIgnoreCase(
                        tool.approvalStatus
                );
    }

    /*
     * ================================================================
     * POLICY HARD GATE
     * ================================================================
     */

    private boolean policyAllows(
            AiTool tool,
            String dataType
    ) {

        String policy =
                normalize(
                        Optional.ofNullable(
                                tool.dataPolicy
                        ).orElse("")
                );

        String requestedData =
                normalize(dataType);

        /*
         * ------------------------------------------------------------
         * Explicit prohibition always wins.
         * ------------------------------------------------------------
         *
         * Example:
         *
         * PROHIBITED:CUSTOMER_DATA
         *
         * + CUSTOMER_DATA
         *
         * = BLOCK
         */

        if (policy.contains(
                "prohibited:" + requestedData
        )) {

            return false;
        }

        /*
         * ------------------------------------------------------------
         * Explicit policy types.
         * ------------------------------------------------------------
         */

        if (Set.of(
                "internal",
                "customer_data",
                "public"
        ).contains(policy)) {

            return policy.equals(
                    requestedData
            );
        }

        /*
         * ------------------------------------------------------------
         * Unknown policy:
         *
         * Fail closed.
         * ------------------------------------------------------------
         */

        return false;
    }

    /*
     * ================================================================
     * SCORING
     * ================================================================
     */

    private int score(
            AiTool tool,
            String requestedCapability,
            String dataType
    ) {

        int capabilityScore =
                capabilityMatches(
                        tool,
                        requestedCapability
                )
                        ? 55
                        : 0;

        int approvalScore =
                isApproved(tool)
                        ? 15
                        : 0;

        int policyScore =
                policyAllows(
                        tool,
                        dataType
                )
                        ? 15
                        : 0;

        int riskScore =
                riskScore(
                        tool.riskLevel
                );

        /*
         * Maximum:
         *
         * 55 + 15 + 15 + 15 = 100
         */
        return Math.min(
                100,
                capabilityScore
                        + approvalScore
                        + policyScore
                        + riskScore
        );
    }

    private int riskScore(
            String risk
    ) {

        if (risk == null) {
            return 0;
        }

        return switch (
                risk.toUpperCase(Locale.ROOT)
        ) {

            case "LOW" -> 15;

            case "MEDIUM" -> 10;

            case "HIGH" -> 3;

            default -> 0;
        };
    }

    /*
     * ================================================================
     * CONFIDENCE
     * ================================================================
     */

    private String confidence(
            int score
    ) {

        if (score >= 80) {
            return "HIGH";
        }

        if (score >= 60) {
            return "MEDIUM";
        }

        return "LOW";
    }

    /*
     * ================================================================
     * EXPLANATION
     * ================================================================
     */

    private List<String> buildReasons(
            AiTool tool,
            String capability
    ) {

        List<String> reasons =
                new ArrayList<>();

        reasons.add(
                "Capability matches the requested task"
        );

        reasons.add(
                "Tool is approved"
        );

        reasons.add(
                "Policy hard-check passed"
        );

        if ("LOW".equalsIgnoreCase(
                tool.riskLevel
        )) {

            reasons.add(
                    "Low-risk capability selected"
            );

        } else {

            reasons.add(
                    "Risk and governance constraints were considered"
            );
        }

        reasons.add(
                "Recommendation is explainable and can be reviewed by IT/Security"
        );

        return reasons;
    }

    /*
     * ================================================================
     * NO-MATCH RESPONSE
     * ================================================================
     */

    private Map<String, Object> noMatch(
            String message,
            List<String> reasons
    ) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "match",
                false
        );

        result.put(
                "confidence",
                "LOW"
        );

        result.put(
                "message",
                message
        );

        result.put(
                "reasons",
                reasons
        );

        return result;
    }

    /*
     * ================================================================
     * STRING UTILITIES
     * ================================================================
     */

    private boolean containsAny(
            String text,
            Set<String> terms
    ) {

        String normalized =
                normalize(text);

        return terms.stream()
                .map(this::normalize)
                .anyMatch(
                        normalized::contains
                );
    }

    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}