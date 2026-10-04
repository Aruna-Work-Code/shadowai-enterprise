package com.aigovernance.tenant;

import com.aigovernance.model.TenantMembership;
import com.aigovernance.repository.TenantMembershipRepository;
import com.aigovernance.repository.TenantRepository;
import com.aigovernance.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves the tenant for every authenticated request.
 *
 * Resolution order:
 *
 * 1. X-Tenant-Id header, if supplied.
 * 2. Otherwise the user's first/default membership.
 *
 * The requested tenant is always validated against the
 * authenticated user's tenant membership.
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    private final TenantRepository tenantRepository;
    private final TenantMembershipRepository membershipRepository;
    private final UserRepository userRepository;

    public TenantContextFilter(
            TenantRepository tenantRepository,
            TenantMembershipRepository membershipRepository,
            UserRepository userRepository
    ) {
        this.tenantRepository = tenantRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            Authentication authentication =
                    SecurityContextHolder
                            .getContext()
                            .getAuthentication();

            /*
             * Public endpoints and anonymous requests
             * do not require tenant resolution.
             */
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getPrincipal() == null
                    || "anonymousUser".equals(authentication.getPrincipal())) {

                filterChain.doFilter(request, response);
                return;
            }

            String username = authentication.getName();

            if (username == null || username.isBlank()) {
                filterChain.doFilter(request, response);
                return;
            }

            /*
             * JWT subject -> application User.
             */
            var user =
                    userRepository
                            .findByUsername(username)
                            .orElse(null);

            if (user == null) {
                writeForbidden(
                        response,
                        "Authenticated user was not found"
                );
                return;
            }

            Long userId = user.id;

            /*
             * Resolve requested tenant.
             */
            Long tenantId =
                    resolveTenantId(request, userId);

            if (tenantId == null) {
                writeForbidden(
                        response,
                        "No tenant context available"
                );
                return;
            }

            /*
             * Verify that the tenant exists.
             */
            if (!tenantRepository.existsById(tenantId)) {
                writeForbidden(
                        response,
                        "Tenant does not exist"
                );
                return;
            }

            /*
             * CRITICAL SECURITY CHECK:
             *
             * The authenticated user must belong
             * to the requested tenant.
             */
            TenantMembership membership =
                    membershipRepository
                            .findByTenantIdAndUserId(
                                    tenantId,
                                    userId
                            )
                            .orElse(null);

            if (membership == null) {
                writeForbidden(
                        response,
                        "User is not a member of this tenant"
                );
                return;
            }

            /*
             * Establish request-scoped tenant context.
             */
            TenantContext.set(tenantId);

            filterChain.doFilter(request, response);

        } finally {

            /*
             * Always clear ThreadLocal state.
             *
             * This prevents tenant context leaking
             * into another request/thread.
             */
            TenantContext.clear();
        }
    }

    private Long resolveTenantId(
            HttpServletRequest request,
            Long userId
    ) {

        String header =
                request.getHeader(TENANT_HEADER);

        /*
         * Explicit tenant requested.
         */
        if (header != null && !header.isBlank()) {

            try {

                return Long.parseLong(
                        header.trim()
                );

            } catch (NumberFormatException ignored) {

                return null;
            }
        }

        /*
         * No X-Tenant-Id header.
         *
         * Use the user's first membership.
         */
        return membershipRepository
                .findFirstByUserId(userId)
                .map(membership -> membership.tenantId)
                .orElse(null);
    }

    private void writeForbidden(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write(
                "{\"error\":\""
                        + escapeJson(message)
                        + "\"}"
        );
    }

    private String escapeJson(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}