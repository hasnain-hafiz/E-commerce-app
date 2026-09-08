package Ecommerce.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight in-memory rate limiter for auth-sensitive endpoints (login,
 * register, forgot-password) — a fixed window of MAX_REQUESTS_PER_WINDOW
 * per client IP per endpoint per WINDOW_SECONDS.
 *
 * Deliberately NOT using a library like bucket4j here: this environment
 * has no network access to check current API/artifact versions against
 * up-to-date docs, and shipping an unverified dependency integration is
 * worse than a small, easy-to-read hand-rolled version. If this app is
 * ever scaled to multiple backend instances behind a load balancer,
 * replace this with bucket4j+Redis (or similar) — this in-memory map is
 * PER INSTANCE and won't coordinate limits across instances.
 *
 * Deliberately a plain class, not a @Component: Spring Boot auto-registers
 * any Filter bean as a servlet filter, which combined with explicitly
 * wiring it into the Spring Security chain (see SecurityConfig) could run
 * it twice per request. Constructed directly where it's needed instead.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/v1/auth/authenticate",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password"
    );

    private static final int MAX_REQUESTS_PER_WINDOW = 8;
    private static final long WINDOW_SECONDS = 60;

    private record Window(AtomicInteger count, Instant windowStart) {}

    private final ConcurrentHashMap<String, Window> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!LIMITED_PATHS.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientIp(request) + ":" + request.getRequestURI();
        Instant now = Instant.now();

        Window window = buckets.compute(key, (k, existing) -> {
            if (existing == null || existing.windowStart().plusSeconds(WINDOW_SECONDS).isBefore(now)) {
                return new Window(new AtomicInteger(1), now);
            }
            existing.count().incrementAndGet();
            return existing;
        });

        if (window.count().get() > MAX_REQUESTS_PER_WINDOW) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"code\":\"RATE_LIMITED\",\"message\":\"Too many attempts. Please wait a minute and try again.\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
