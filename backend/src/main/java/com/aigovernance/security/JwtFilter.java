package com.aigovernance.security;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;

    public JwtFilter(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header =
                request.getHeader("Authorization");

        /*
         * No JWT.
         *
         * Continue because the endpoint may be public.
         */
        if (header == null
                || !header.startsWith("Bearer ")) {

            chain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                header.substring(7).trim();

        if (token.isBlank()) {

            SecurityContextHolder.clearContext();

            chain.doFilter(
                    request,
                    response
            );

            return;
        }

        try {

            Claims claims =
                    jwt.parse(token);

            String username =
                    claims.getSubject();

            String role =
                    claims.get(
                            "role",
                            String.class
                    );

            /*
             * A valid token must contain both
             * username and role.
             */
            if (username == null
                    || username.isBlank()
                    || role == null
                    || role.isBlank()) {

                SecurityContextHolder.clearContext();

                writeUnauthorized(
                        response,
                        "Invalid authentication token"
                );

                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            List.of(
                                    new SimpleGrantedAuthority(
                                            "ROLE_" + role
                                    )
                            )
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            authentication
                    );

        } catch (Exception ex) {

            SecurityContextHolder.clearContext();

            writeUnauthorized(
                    response,
                    "Invalid or expired authentication token"
            );

            return;
        }

        chain.doFilter(
                request,
                response
        );
    }

    private void writeUnauthorized(
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.getWriter().write(
                "{\"error\":\""
                        + message
                        + "\"}"
        );
    }
}