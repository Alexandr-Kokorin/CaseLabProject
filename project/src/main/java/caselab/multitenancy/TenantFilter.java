package caselab.multitenancy;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
            addProblemDetailToResponse(
                request,
                response,
                new Object[] {tenantHeader}
            );
            log.warn("X-TENANT-ID header is missing or empty.");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            log.debug("Tenant context cleared after request processing.");
        }
    }

    private void addProblemDetailToResponse(
        HttpServletRequest request, HttpServletResponse response, Object[] args
    ) throws IOException {
        var problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            Objects.requireNonNull(messageSource.getMessage(
                "tenant.id.invalid_format", args,
                "tenant.id.invalid_format", request.getLocale()
            ))
        );

        problemDetail.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setCharacterEncoding(StandardCharsets.UTF_8.displayName());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
