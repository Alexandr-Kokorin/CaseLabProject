package caselab.configuration.security;

import caselab.domain.entity.ApplicationUser;
import caselab.domain.entity.GlobalPermission;
import caselab.domain.entity.enums.GlobalPermissionName;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationActivationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

                ApplicationUser user = (ApplicationUser) principal;

                boolean isAdmin = user.getGlobalPermissions()
                    .stream()
                    .map(GlobalPermission::getName)
                    .anyMatch(it -> it.equals(GlobalPermissionName.ADMIN));

                if (!isAdmin) {
                    if (user.getOrganization() == null) {
                        log.warn("Запрос заблокирован: У пользователя '{}' отсутствует организация.", user.getEmail());
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.getWriter().write("User has no organization.");
                        return;
                    } else if (!user.getOrganization().isActive()) {
                        log.warn("Запрос заблокирован: Организация '{}' для пользователя '{}' деактивизирована.",
                            user.getOrganization().getName(), user.getEmail());
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.getWriter().write("Organization is deactivated.");
                        return;
                    }
                }
        }

        filterChain.doFilter(request, response);
    }
}
