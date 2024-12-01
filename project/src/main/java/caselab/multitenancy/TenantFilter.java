package caselab.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final MessageSource messageSource;

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull
    FilterChain filterChain
    )
        throws ServletException, IOException {
        var tenantHeader = request.getHeader("X-TENANT-ID");

        if (tenantHeader != null && !tenantHeader.isEmpty()) {
            TenantContext.setTenantId(tenantHeader);
            log.debug("TenantID is set in the context and is equal: {}", TenantContext.getTenantId());
        } else {
            log.warn("X-TENANT-ID header is missing or empty.");
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            log.debug("Tenant context cleared after request processing.");
        }
    }
}
