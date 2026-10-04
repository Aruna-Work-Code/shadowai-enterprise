package com.aigovernance.model;

import com.aigovernance.tenant.TenantContext;
import jakarta.persistence.PrePersist;

/**
 * Assigns the current tenant to tenant-owned entities.
 * Existing records are backfilled by Flyway. New records must have a
 * tenant context; for local demo/webhook compatibility the context falls
 * back to the configured default tenant.
 */
public class TenantEntityListener {
    @PrePersist
    public void assignTenant(Object entity) {
        try {
            var field = entity.getClass().getField("tenantId");
            if (field.get(entity) == null) {
                field.set(entity, TenantContext.currentOrDefault());
            }
        } catch (NoSuchFieldException ignored) {
            // Entity is not tenant-owned.
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to assign tenant", e);
        }
    }
}
