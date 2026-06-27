package com.ces.exam.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight per-IP, fixed-window rate limiter for the unauthenticated surface
 * (login + public exam-token endpoints). Guards against credential/token brute
 * force and basic request floods. In-memory: sufficient for the single-instance
 * deployment; swap for a Redis bucket if the app is ever horizontally scaled.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MS = 60_000;       // 1-minute window
    private static final int AUTH_LIMIT = 30;           // login/auth attempts per IP per window
    // Generous: many candidates can share one corporate/NAT egress IP. Still far below
    // what a brute-force/flood needs, and tokens are unguessable UUIDs regardless.
    private static final int PUBLIC_LIMIT = 300;        // public exam-token calls per IP per window

    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    private record Counter(long windowStart, AtomicInteger count) {}

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return !(p.startsWith("/api/v1/auth/") || p.startsWith("/api/v1/public/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = path.startsWith("/api/v1/auth/") ? AUTH_LIMIT : PUBLIC_LIMIT;
        String key = clientIp(request) + "|" + (path.startsWith("/api/v1/auth/") ? "auth" : "public");

        if (isOverLimit(key, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Çox sayda sorğu. Bir az gözləyin və yenidən cəhd edin.\"}");
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isOverLimit(String key, int limit) {
        long now = System.currentTimeMillis();
        Counter updated = counters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart() >= WINDOW_MS) {
                return new Counter(now, new AtomicInteger(1));
            }
            existing.count().incrementAndGet();
            return existing;
        });
        // Opportunistic cleanup so the map can't grow unbounded.
        if (counters.size() > 10_000) {
            counters.entrySet().removeIf(e -> now - e.getValue().windowStart() >= WINDOW_MS);
        }
        return updated.count().get() > limit;
    }

    /** Real client IP behind nginx/Cloudflare; falls back to the socket address. */
    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }
}
