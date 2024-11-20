package caselab.multitenancy.interceptor;

import caselab.multitenancy.util.TenantContext;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

@Component
public class TenantInterceptor implements WebRequestInterceptor {

    @Override
    public void preHandle(WebRequest request) throws Exception {
        if (Objects.nonNull(request.getHeader("X-TENANT-ID"))) {
            String tenantId = request.getHeader("X-TENANT-ID");
            TenantContext.setTenantId(tenantId);
        }
    }

    @Override
    public void postHandle(@NotNull WebRequest request, ModelMap model) throws Exception {
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(@NotNull WebRequest request, Exception ex) throws Exception {
        // No operations
    }
}
