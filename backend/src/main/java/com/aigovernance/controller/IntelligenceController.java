package com.aigovernance.controller;

import com.aigovernance.intelligence.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/intelligence")
public class IntelligenceController {
    private final IntentIntelligenceService intent;
    private final DecisionExplanationService explanation;

    public IntelligenceController(IntentIntelligenceService intent,
                                   DecisionExplanationService explanation) {
        this.intent=intent; this.explanation=explanation;
    }

    @PostMapping("/intent")
    public IntentAnalysis intent(@RequestBody IntentBody body) {
        return intent.analyze(body.text(), body.dataType());
    }

    public record IntentBody(String text, String dataType) {}
}
