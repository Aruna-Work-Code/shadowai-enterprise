package com.aigovernance.decision;

import com.aigovernance.model.AiTool;
import com.aigovernance.model.Policy;
import com.aigovernance.model.RequestEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DecisionEngine {

    public DecisionResult evaluate(
            RequestEntity request,
            List<AiTool> tools,
            List<Policy> policies
    ) {

        PolicyEvaluation evaluation =
                evaluatePolicies(request, policies);

        /*
         * Hard policy constraints are evaluated before
         * candidate generation.
         *
         * Example:
         * CUSTOMER_DATA currently requires explicit
         * governance exception handling.
         */
        if (!evaluation.allowed()) {

            return new DecisionResult(
                    "BLOCKED",
                    null,
                    0,
                    "HIGH",
                    evaluation.riskLevel(),
                    evaluation.reasons(),
                    evaluation.constraints()
            );
        }

        /*
         * Generate eligible approved tools.
         */
        List<ToolCandidate> candidates =
                generateCandidates(request, tools);

        if (candidates.isEmpty()) {

            return new DecisionResult(
                    "NO_APPROVED_ALTERNATIVE",
                    null,
                    0,
                    "LOW",
                    evaluation.riskLevel(),
                    List.of(
                            "No eligible approved AI capability was found"
                    ),
                    evaluation.constraints()
            );
        }

        /*
         * Select the highest-scoring candidate.
         */
        ToolCandidate best =
                candidates.stream()
                        .max(
                                Comparator.comparingInt(
                                        ToolCandidate::score
                                )
                        )
                        .orElseThrow();

        String confidence =
                best.score() >= 85
                        ? "HIGH"
                        : best.score() >= 65
                        ? "MEDIUM"
                        : "LOW";

        return new DecisionResult(
                "RECOMMEND",
                best.toolName(),
                best.score(),
                confidence,
                evaluation.riskLevel(),
                buildReasons(
                        best,
                        evaluation
                ),
                evaluation.constraints()
        );
    }

    /**
     * Evaluate governance policies before
     * recommending any AI capability.
     */
    private PolicyEvaluation evaluatePolicies(
            RequestEntity request,
            List<Policy> policies
    ) {

        List<String> constraints =
                new ArrayList<>();

        List<String> reasons =
                new ArrayList<>();

        String dataType =
                request.dataType == null
                        ? ""
                        : request.dataType.toUpperCase();

        /*
         * Customer data is currently a hard governance
         * constraint and therefore requires an exception.
         */
        if ("CUSTOMER_DATA".equals(dataType)) {

            constraints.add(
                    "CUSTOMER_DATA requires explicit governance approval"
            );

            reasons.add(
                    "Request contains customer data"
            );

            return new PolicyEvaluation(
                    false,
                    "HIGH",
                    constraints,
                    reasons
            );
        }

        /*
         * Confidential data is allowed to proceed through
         * normal policy evaluation, but carries additional risk.
         */
        if ("CONFIDENTIAL".equals(dataType)) {

            constraints.add(
                    "CONFIDENTIAL data requires policy evaluation"
            );

            reasons.add(
                    "Confidential data increases governance risk"
            );
        }

        /*
         * Internal data can use approved enterprise tools.
         */
        if ("INTERNAL".equals(dataType)) {

            reasons.add(
                    "Internal data is eligible for approved enterprise capabilities"
            );
        }

        return new PolicyEvaluation(
                true,
                "LOW",
                constraints,
                reasons
        );
    }

    /**
     * Generate tool candidates.
     *
     * IMPORTANT:
     * AiTool does not contain an "enabled" property.
     *
     * The existing system uses approvalStatus to determine
     * whether a tool is approved for recommendation.
     */
    private List<ToolCandidate> generateCandidates(
            RequestEntity request,
            List<AiTool> tools
    ) {

        List<ToolCandidate> candidates =
                new ArrayList<>();

        String intent =
                request.intent == null
                        ? ""
                        : request.intent.toLowerCase();

        for (AiTool tool : tools) {

            /*
             * Only approved tools can be recommended.
             *
             * We intentionally use the existing
             * AiTool.approvalStatus field instead of
             * introducing a new database field.
             */
            if (tool.approvalStatus == null
                    || !"APPROVED".equalsIgnoreCase(
                            tool.approvalStatus
                    )) {

                continue;
            }

            int score = 50;

            String reason =
                    "Approved capability available";

            /*
             * Software development / engineering intent.
             */
            if (intent.contains("java")
                    || intent.contains("api")
                    || intent.contains("debug")
                    || intent.contains("unit test")
                    || intent.contains("code")
                    || intent.contains("programming")) {

                if (tool.name != null
                        && tool.name.toLowerCase()
                        .contains("code")) {

                    score += 40;

                    reason =
                            "Tool matches software development intent";
                }
            }

            /*
             * Research / summarization intent.
             */
            if (intent.contains("research")
                    || intent.contains("summarize")
                    || intent.contains("summary")
                    || intent.contains("complaint")) {

                if (tool.name != null
                        && tool.name.toLowerCase()
                        .contains("enterprise")) {

                    score += 40;

                    reason =
                            "Tool matches research and summarization intent";
                }
            }

            /*
             * Capability metadata can provide an additional
             * signal without changing the database schema.
             */
            if (tool.capabilities != null
                    && !tool.capabilities.isBlank()) {

                String capabilities =
                        tool.capabilities.toLowerCase();

                if ((intent.contains("java")
                        || intent.contains("api")
                        || intent.contains("debug")
                        || intent.contains("code"))
                        && (capabilities.contains("code")
                        || capabilities.contains("developer")
                        || capabilities.contains("programming"))) {

                    score += 5;

                    reason =
                            "Approved capability matches software development requirements";
                }

                if ((intent.contains("research")
                        || intent.contains("summarize")
                        || intent.contains("complaint"))
                        && (capabilities.contains("research")
                        || capabilities.contains("summar")
                        || capabilities.contains("analysis"))) {

                    score += 5;

                    reason =
                            "Approved capability matches research and analysis requirements";
                }
            }

            /*
             * Never allow a score above 100.
             */
            score =
                    Math.min(score, 100);

            candidates.add(
                    new ToolCandidate(
                            tool.id,
                            tool.name,
                            score,
                            "LOW",
                            reason
                    )
            );
        }

        return candidates;
    }

    /**
     * Build human-readable explanation for the
     * recommendation.
     */
    private List<String> buildReasons(
            ToolCandidate candidate,
            PolicyEvaluation evaluation
    ) {

        List<String> reasons =
                new ArrayList<>();

        reasons.addAll(
                evaluation.reasons()
        );

        reasons.add(
                candidate.reason()
        );

        reasons.add(
                "Recommendation is based on deterministic governance rules"
        );

        return reasons;
    }
}