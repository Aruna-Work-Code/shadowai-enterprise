package com.aigovernance.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMIT = 120;
    private static final long WINDOW_SECONDS = 60;

    private record Bucket(long window, int count) {}

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/actuator")
                || request.getRequestURI().startsWith("/swagger-ui")
                || request.getRequestURI().startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr();
        long window = Instant.now().getEpochSecond() / WINDOW_SECONDS;

        Bucket old = buckets.get(key);
        Bucket next;

        if (old == null || old.window() != window) {
            next = new Bucket(window, 1);
        } else {
            next = new Bucket(window, old.count() + 1);
        }

        buckets.put(key, next);

        response.setHeader("X-RateLimit-Limit", String.valueOf(LIMIT));
        response.setHeader("X-RateLimit-Remaining",
                String.valueOf(Math.max(0, LIMIT - next.count())));

        if (next.count() > LIMIT) {
            response.setStatus(429);
            response.setHeader("Retry-After",
                    String.valueOf(WINDOW_SECONDS));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
