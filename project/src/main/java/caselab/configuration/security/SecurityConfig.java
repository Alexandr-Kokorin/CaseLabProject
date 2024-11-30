package caselab.configuration.security;

import caselab.multitenancy.TenantFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final TenantFilter tenantFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfiguration = new CorsConfiguration();
                corsConfiguration.setAllowedOriginPatterns(List.of("*"));
                corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
                corsConfiguration.setAllowedHeaders(List.of("*"));
                corsConfiguration.setAllowCredentials(true);
                return corsConfiguration;
            }))
            .authorizeHttpRequests(matcherRegistry ->
                matcherRegistry
                    .requestMatchers("/api/v1/auth/authenticate").permitAll() // запрос не требует аутентификации
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/api/v1/auth/refresh-token").permitAll()
                    .requestMatchers("/api/v1/organizations/register").permitAll()
                    .anyRequest().authenticated()) // запрос требует аутентификацию
            .sessionManagement(sessionManagementConfigurer ->
                sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(tenantFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
