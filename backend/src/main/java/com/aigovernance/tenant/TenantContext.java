package com.aigovernance.tenant;

/**
 * Request-scoped tenant context backed by a ThreadLocal.
 * Always cleared by TenantContextFilter.
 */
public final class TenantContext {
    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();
    private static final long DEFAULT_TENANT_ID = 1L;

    private TenantContext() {}

    public static void set(Long tenantId) { CURRENT.set(tenantId); }
    public static Long current() { return CURRENT.get(); }
    public static long currentOrDefault() {
        Long id = CURRENT.get();
        return id == null ? DEFAULT_TENANT_ID : id;
    }
    public static void clear() { CURRENT.remove(); }
}
