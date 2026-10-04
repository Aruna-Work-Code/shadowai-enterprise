package com.aigovernance.integration.provider;

/** Pluggable enterprise provider adapter. Governance decisions remain local. */
public interface ExternalProviderAdapter {
    String provider();
    AdapterResult validate(String baseUrl, String credential);
    record AdapterResult(boolean success, int status, String message) {}
}
