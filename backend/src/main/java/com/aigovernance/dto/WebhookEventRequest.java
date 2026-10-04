package com.aigovernance.dto;
import jakarta.validation.constraints.*;
public record WebhookEventRequest(@NotBlank String provider,@NotBlank String providerEventId,
 @NotBlank String eventType,@NotBlank @Size(max=10000) String payload){}
