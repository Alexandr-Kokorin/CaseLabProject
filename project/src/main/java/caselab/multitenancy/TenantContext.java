package caselab.multitenancy;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TenantContext {

    private TenantContext() {
    }

    private static final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        log.debug("Setting tenantId to {}", tenantId);
        currentTenant.set(tenantId);
    }

    public static String getTenantId() {
        return currentTenant.get();
    }

    public static void clear() {
        log.debug("Clearing the tenant ID from the context.");
        currentTenant.remove();
    }
}
