package com.aigovernance.service;

import com.aigovernance.dto.WebhookEventRequest;
import com.aigovernance.model.IntegrationConnection;
import com.aigovernance.model.IntegrationEvent;
import com.aigovernance.model.Investigation;
import com.aigovernance.repository.IntegrationConnectionRepository;
import com.aigovernance.repository.IntegrationEventRepository;
import com.aigovernance.repository.InvestigationRepository;
import com.aigovernance.tenant.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Integration ingestion and governance signal processing.
 */
@Service
public class IntegrationService {

    private final IntegrationConnectionRepository connections;
    private final IntegrationEventRepository events;
    private final InvestigationRepository investigations;
    private final AuditService audit;
    private final PasswordEncoder encoder;
    private final EventStreamService eventStream;
    private final NotificationService notificationService;

    public IntegrationService(
            IntegrationConnectionRepository connections,
            IntegrationEventRepository events,
            InvestigationRepository investigations,
            AuditService audit,
            PasswordEncoder encoder,
            EventStreamService eventStream,
            NotificationService notificationService
    ) {
        this.connections = connections;
        this.events = events;
        this.investigations = investigations;
        this.audit = audit;
        this.encoder = encoder;
        this.eventStream = eventStream;
        this.notificationService = notificationService;
    }

    /**
     * Create an integration connection.
     *
     * The raw secret is never stored.
     */
    public IntegrationConnection create(
            String name,
            String provider,
            String secret,
            Long actor
    ) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Integration name is required"
            );
        }

        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException(
                    "Integration provider is required"
            );
        }

        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "Integration secret is required"
            );
        }

        IntegrationConnection connection =
                new IntegrationConnection();

        connection.name = name.trim();
        connection.provider = provider.trim().toUpperCase();
        connection.createdBy = actor;
        connection.enabled = true;

        /*
         * Store only a BCrypt hash.
         */
        connection.webhookSecretHash =
                encoder.encode(secret);

        return connections.save(connection);
    }

    /**
     * Receive an external webhook event.
     *
     * Authentication:
     *
     * X-Integration-Secret
     *
     * Processing:
     *
     * webhook
     *    ↓
     * integration event
     *    ↓
     * governance signal
     *    ↓
     * investigation
     *    ↓
     * audit / notification / event stream
     */
    public IntegrationEvent receive(
            WebhookEventRequest request,
            String secret
    ) {

        IntegrationConnection connection =
                connections.findAll()
                        .stream()
                        .filter(c ->
                                c.enabled
                                        && c.provider != null
                                        && c.provider.equalsIgnoreCase(
                                        request.provider()
                                )
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No enabled integration for provider"
                                )
                        );

        TenantContext.set(connection.tenantId);

        try {

        /*
         * Verify the external secret.
         */
        if (secret == null
                || !encoder.matches(
                secret,
                connection.webhookSecretHash
        )) {

            throw new IllegalArgumentException(
                    "Invalid integration secret"
            );
        }

        /*
         * Idempotency.
         *
         * The same external event must never generate
         * two investigations.
         */
        var existing =
                events.findByTenantIdAndProviderAndProviderEventId(TenantContext.currentOrDefault(), 
                        request.provider(),
                        request.providerEventId()
                );

        if (existing.isPresent()) {
            return existing.get();
        }

        IntegrationEvent event =
                new IntegrationEvent();

        event.provider =
                request.provider();

        event.providerEventId =
                request.providerEventId();

        event.eventType =
                request.eventType();

        event.payload =
                request.payload();

        event.processingStatus =
                "RECEIVED";

        event = events.save(event);

        try {

            /*
             * Phase 8 governance signal:
             *
             * External system reports unmanaged AI usage.
             */
            if ("UNMANAGED_AI_USAGE"
                    .equalsIgnoreCase(
                            request.eventType()
                    )) {

                Investigation investigation =
                        new Investigation();

                investigation.title =
                        "External signal: unmanaged AI usage";

                investigation.toolName =
                        extract(
                                request.payload(),
                                "toolName",
                                "Unknown tool"
                        );

                investigation.department =
                        extract(
                                request.payload(),
                                "department",
                                "UNKNOWN"
                        );

                investigation.riskLevel =
                        extract(
                                request.payload(),
                                "riskLevel",
                                "HIGH"
                        );

                investigation.status =
                        "OPEN";

                investigation.businessContext =
                        extract(
                                request.payload(),
                                "businessContext",
                                "External governance signal."
                        );

                investigation.rootCause =
                        "External signal requires governance review.";

                investigation.recommendedAction =
                        "Review policy fit, identify an approved alternative and record a human decision.";

                investigation =
                        investigations.save(
                                investigation
                        );

                /*
                 * Audit.
                 */
                audit.record(
                        "INVESTIGATION",
                        investigation.id,
                        "CREATED_FROM_INTEGRATION",
                        null,
                        "event=" + event.id
                );

                /*
                 * Notification.
                 */
                notificationService.create(
                        null,
                        "INVESTIGATION",
                        "New governance investigation",
                        "Unmanaged AI usage requires review: "
                                + investigation.toolName
                );

                /*
                 * Real-time dashboard/event stream.
                 */
                eventStream.publish(
                        "investigation.created",
                        Map.of(
                                "id",
                                investigation.id,
                                "tool",
                                investigation.toolName,
                                "risk",
                                investigation.riskLevel
                        )
                );
            }

            event.processingStatus =
                    "PROCESSED";

            event.processedAt =
                    Instant.now();

        } catch (Exception ex) {

            event.processingStatus =
                    "FAILED";

            event.errorMessage =
                    ex.getMessage();

            audit.record(
                    "INTEGRATION_EVENT",
                    event.id,
                    "PROCESSING_FAILED",
                    null,
                    ex.getMessage()
            );
        }

        return events.save(event);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Minimal JSON extraction for the current synthetic webhook
     * contract.
     *
     * This is intentionally kept dependency-free.
     *
     * For production integrations we should replace this with
     * Jackson ObjectMapper parsing.
     */
    private String extract(
            String json,
            String key,
            String fallback
    ) {

        if (json == null) {
            return fallback;
        }

        String marker =
                "\"" + key + "\"";

        int position =
                json.indexOf(marker);

        if (position < 0) {
            return fallback;
        }

        int colon =
                json.indexOf(
                        ':',
                        position
                );

        if (colon < 0) {
            return fallback;
        }

        int quoteStart =
                json.indexOf(
                        '"',
                        colon + 1
                );

        if (quoteStart < 0) {
            return fallback;
        }

        int quoteEnd =
                json.indexOf(
                        '"',
                        quoteStart + 1
                );

        if (quoteEnd < 0) {
            return fallback;
        }

        return json.substring(
                quoteStart + 1,
                quoteEnd
        );
    }
}