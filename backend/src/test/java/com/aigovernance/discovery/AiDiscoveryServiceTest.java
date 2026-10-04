package com.aigovernance.discovery;

import com.aigovernance.model.AiTool;
import com.aigovernance.model.Investigation;
import com.aigovernance.repository.AiToolRepository;
import com.aigovernance.repository.InvestigationRepository;
import com.aigovernance.service.AuditService;
import com.aigovernance.service.EventStreamService;
import com.aigovernance.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiDiscoveryServiceTest {
    private AiToolRepository tools;
    private InvestigationRepository investigations;
    private AuditService audit;
    private NotificationService notifications;
    private EventStreamService events;
    private AiDiscoveryService service;

    @BeforeEach
    void setUp() {
        tools = mock(AiToolRepository.class);
        investigations = mock(InvestigationRepository.class);
        audit = mock(AuditService.class);
        notifications = mock(NotificationService.class);
        events = mock(EventStreamService.class);
        service = new AiDiscoveryService(tools, investigations, audit, notifications, events);
    }

    @Test
    void unknownToolBecomesUnapprovedAndCreatesInvestigation() {
        when(tools.findByTenantIdAndNameIgnoreCase(anyLong(), eq("Shadow Research AI")))
                .thenReturn(Optional.empty());
        when(tools.save(any(AiTool.class))).thenAnswer(inv -> {
            AiTool t = inv.getArgument(0);
            t.id = 101L;
            return t;
        });
        when(investigations.findFirstByTenantIdAndToolNameAndStatusOrderByIdDesc(anyLong(), eq("Shadow Research AI"), eq("OPEN")))
                .thenReturn(Optional.empty());
        when(investigations.save(any(Investigation.class))).thenAnswer(inv -> {
            Investigation i = inv.getArgument(0);
            i.id = 501L;
            return i;
        });

        var result = service.discover(new AiDiscoveryService.DiscoveryRequest(
                "Shadow Research AI", "Research", "Summarization", "HIGH",
                "UNKNOWN", "UNASSIGNED", "SUPPORT", "Customer complaint analysis"));

        assertEquals("UNAPPROVED", result.approvalStatus());
        assertEquals("HIGH", result.riskLevel());
        assertTrue(result.newlyRegistered());
        assertEquals(501L, result.investigationId());
        verify(investigations).save(any(Investigation.class));
        verify(notifications).create(isNull(), eq("AI_TOOL_DISCOVERY"), anyString(), anyString());
        verify(events).publish(eq("ai-tool.discovered"), any());
    }

    @Test
    void approvedToolDoesNotCreateInvestigation() {
        AiTool existing = new AiTool();
        existing.id = 22L;
        existing.name = "Approved Copilot";
        existing.approvalStatus = "APPROVED";
        existing.riskLevel = "LOW";
        when(tools.findByTenantIdAndNameIgnoreCase(anyLong(), eq("Approved Copilot")))
                .thenReturn(Optional.of(existing));

        var result = service.discover(new AiDiscoveryService.DiscoveryRequest(
                "Approved Copilot", "Coding", null, "LOW", "COMPANY_APPROVED", "IT",
                "ENGINEERING", "Coding assistance"));

        assertEquals("APPROVED", result.approvalStatus());
        assertFalse(result.newlyRegistered());
        assertNull(result.investigationId());
        verify(investigations, never()).save(any());
        verify(notifications, never()).create(any(), anyString(), anyString(), anyString());
    }
}
