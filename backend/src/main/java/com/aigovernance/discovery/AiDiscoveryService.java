package com.aigovernance.discovery;

import com.aigovernance.model.AiTool;
import com.aigovernance.model.Investigation;
import com.aigovernance.repository.AiToolRepository;
import com.aigovernance.repository.InvestigationRepository;
import com.aigovernance.service.AuditService;
import com.aigovernance.service.EventStreamService;
import com.aigovernance.service.NotificationService;
import com.aigovernance.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shadow-AI discovery workflow.
 *
 * It converts an observed AI tool name into a governed inventory record.
 * Unknown tools are registered as UNAPPROVED/HIGH risk and create an
 * investigation so that a human governance owner can make the final decision.
 */
@Service
public class AiDiscoveryService {

    private final AiToolRepository tools;
    private final InvestigationRepository investigations;
    private final AuditService audit;
    private final NotificationService notifications;
    private final EventStreamService events;

    public AiDiscoveryService(
            AiToolRepository tools,
            InvestigationRepository investigations,
            AuditService audit,
            NotificationService notifications,
            EventStreamService events) {
        this.tools = tools;
        this.investigations = investigations;
        this.audit = audit;
        this.notifications = notifications;
        this.events = events;
    }

    public DiscoveryResult discover(DiscoveryRequest request) {
        if (request == null || request.toolName() == null || request.toolName().isBlank()) {
            throw new IllegalArgumentException("toolName is required");
        }

        long tenantId = TenantContext.currentOrDefault();
        String toolName = request.toolName().trim();

        AiTool tool = tools.findByTenantIdAndNameIgnoreCase(tenantId, toolName).orElse(null);
        boolean newlyRegistered = false;

        if (tool == null) {
            tool = new AiTool();
            tool.tenantId = tenantId;
            tool.name = toolName;
            tool.category = blankTo(request.category(), "UNKNOWN");
            tool.capabilities = blankTo(request.capabilities(), "Discovered externally; capabilities not yet verified.");
            tool.approvalStatus = "UNAPPROVED";
            tool.riskLevel = normalizeRisk(request.riskLevel());
            tool.dataPolicy = blankTo(request.dataPolicy(), "UNKNOWN");
            tool.owner = blankTo(request.owner(), "UNASSIGNED");
            tool = tools.save(tool);
            newlyRegistered = true;
        }

        boolean approved = "APPROVED".equalsIgnoreCase(tool.approvalStatus);
        Investigation investigation = null;

        if (!approved) {

    investigation = investigations
            .findFirstByTenantIdAndToolNameAndStatusOrderByIdDesc(
                    tenantId,
                    tool.name,
                    "OPEN"
            )
            .orElse(null);

    if (investigation == null) {
        investigation = createInvestigation(tool, request);
    }
}

        audit.record(
                "AI_TOOL",
                tool.id,
                newlyRegistered ? "DISCOVERED_UNAPPROVED" : "DISCOVERY_CHECKED",
                null,
                "approvalStatus=" + tool.approvalStatus + ",riskLevel=" + tool.riskLevel);

        if (newlyRegistered && investigation != null) {
            notifications.create(
                    null,
                    "AI_TOOL_DISCOVERY",
                    "New unapproved AI tool detected",
                    tool.name + " requires governance review.");

            events.publish(
                    "ai-tool.discovered",
                    Map.of(
                            "toolId", tool.id,
                            "toolName", tool.name,
                            "approvalStatus", tool.approvalStatus,
                            "riskLevel", tool.riskLevel,
                            "investigationId", investigation.id));
        }

        return new DiscoveryResult(
                tool.id,
                tool.name,
                tool.approvalStatus,
                tool.riskLevel,
                newlyRegistered,
                investigation == null ? null : investigation.id,
                approved ? "Tool is approved for governed use." :
                        "Tool is not approved. Governance review is required.");
    }

    public List<AiTool> inventory() {
        return tools.findByTenantId(TenantContext.currentOrDefault());
    }

    private Investigation createInvestigation(AiTool tool, DiscoveryRequest request) {
        Investigation investigation = new Investigation();
        investigation.tenantId = TenantContext.currentOrDefault();
        investigation.title = "Shadow AI detection: " + tool.name;
        investigation.toolName = tool.name;
        investigation.department = blankTo(request.department(), "UNKNOWN");
        investigation.riskLevel = tool.riskLevel;
        investigation.status = "OPEN";
        investigation.businessContext = blankTo(
                request.businessContext(),
                "AI tool was discovered outside the approved AI inventory.");
        investigation.rootCause = "Observed AI capability is not currently approved in the tenant tool inventory.";
        investigation.recommendedAction =
                "Review policy fit, verify the vendor/data policy, identify an approved alternative, and record a human decision.";
        investigation = investigations.save(investigation);

        audit.record(
                "INVESTIGATION",
                investigation.id,
                "CREATED_FROM_AI_DISCOVERY",
                null,
                "tool=" + tool.name);

        return investigation;
    }

    private static String normalizeRisk(String risk) {
        String value = blankTo(risk, "HIGH").toUpperCase(Locale.ROOT);
        return List.of("LOW", "MEDIUM", "HIGH", "CRITICAL").contains(value) ? value : "HIGH";
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public record DiscoveryRequest(
            String toolName,
            String category,
            String capabilities,
            String riskLevel,
            String dataPolicy,
            String owner,
            String department,
            String businessContext) {}

    public record DiscoveryResult(
            Long toolId,
            String toolName,
            String approvalStatus,
            String riskLevel,
            boolean newlyRegistered,
            Long investigationId,
            String message) {}
}
