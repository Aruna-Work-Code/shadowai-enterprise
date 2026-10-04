package com.aigovernance.intelligence;

import com.aigovernance.decision.DecisionResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DecisionExplanationService {

    /**
     * Creates a deterministic human-readable explanation
     * from the decision engine result.
     *
     * IMPORTANT:
     * This layer explains the decision.
     * It does not override the policy engine.
     */
    public Explanation explain(DecisionResult result) {

        List<String> explanation =
                new ArrayList<>();

        if (result == null) {
            explanation.add(
                    "No decision result was available."
            );

            return new Explanation(
                    "UNKNOWN",
                    null,
                    0,
                    "LOW",
                    "UNKNOWN",
                    explanation
            );
        }

        if (result.reasons() != null) {
            explanation.addAll(
                    result.reasons()
            );
        }

        if (result.constraints() != null
                && !result.constraints().isEmpty()) {

            explanation.add(
                    "Governance constraints:"
            );

            explanation.addAll(
                    result.constraints()
            );
        }

        if (result.recommendedTool() != null
                && !result.recommendedTool().isBlank()) {

            explanation.add(
                    "Recommended capability: "
                            + result.recommendedTool()
            );

            explanation.add(
                    "Recommendation score: "
                            + result.score()
            );

            explanation.add(
                    "Recommendation confidence: "
                            + result.confidence()
            );
        }

        explanation.add(
                "Decision is based on deterministic governance rules."
        );

        return new Explanation(
                result.decision(),
                result.recommendedTool(),
                result.score(),
                result.confidence(),
                result.riskLevel(),
                explanation
        );
    }

    public record Explanation(
            String decision,
            String recommendedTool,
            int score,
            String confidence,
            String riskLevel,
            List<String> reasons
    ) {
    }
}