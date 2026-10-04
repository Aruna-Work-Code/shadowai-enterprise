package com.aigovernance.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import com.aigovernance.tenant.TenantContextFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain chain(
            HttpSecurity http,
            JwtFilter jwt,
            RateLimitFilter rateLimit,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            TenantContextFilter tenantContextFilter,
            CorrelationIdFilter correlationIdFilter
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
                    config.setAllowedMethods(List.of(
                            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                    ));
                    config.setAllowedHeaders(List.of(
                            "Authorization",
                            "Content-Type",
                            "X-Integration-Secret",
                            "X-Integration-Signature",
                            "X-Integration-Timestamp",
                            "X-Correlation-Id",
                            "X-Tenant-Id"
                    ));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data:; " +
                                "connect-src 'self' http://localhost:8080; " +
                                "font-src 'self' data:"))
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(content -> {}))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/v1/events/stream").permitAll()
                        .requestMatchers("/api/v1/webhooks/events").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimit, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantContextFilter, JwtFilter.class)
                .build();
    }
}
