package com.healthcare.erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiter for login endpoint.
 * Blocks an IP after 5 failed attempts within 1 minute.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @org.springframework.beans.factory.annotation.Value("${rate-limit.max-attempts:5}")
    private int maxAttempts;

    @org.springframework.beans.factory.annotation.Value("${rate-limit.window-ms:60000}")
    private long windowMs;

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only rate limit the login endpoint
        if (!"/api/auth/login".equals(request.getRequestURI()) || !"POST".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = getClientIp(request);
        AttemptInfo info = attempts.compute(ip, (key, existing) -> {
            if (existing == null || System.currentTimeMillis() - existing.windowStart > windowMs) {
                return new AttemptInfo();
            }
            return existing;
        });

        if (info.count.incrementAndGet() > maxAttempts) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many login attempts. Please wait 1 minute.\",\"timestamp\":\""
                            + java.time.LocalDateTime.now() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);

        // QA FIX: If authentication was successful, reset the counter for this IP
        if (response.getStatus() == HttpServletResponse.SC_OK) {
            info.count.set(0);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class AttemptInfo {
        final long windowStart = System.currentTimeMillis();
        final AtomicInteger count = new AtomicInteger(0);
    }
}
