package Ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import Ecommerce.utils.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Lightweight sliding-window rate limiter for brute-force-sensitive endpoints
 * (login/register). In-memory, per-instance - fine for a single node; behind
 * a load balancer this should move to Redis (e.g. Bucket4j + Redis) so limits
 * are shared across instances.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_MILLIS = 60_000; // 1 minute

    private final ConcurrentHashMap<String, Deque<Long>> hits = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isRateLimited(request)) {
            String key = clientKey(request);
            Deque<Long> timestamps = hits.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());
            long now = System.currentTimeMillis();

            synchronized (timestamps) {
                while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                    timestamps.pollFirst();
                }
                if (timestamps.size() >= MAX_REQUESTS) {
                    response.setStatus(429);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(
                            new ApiResponse<>("Too many requests. Please try again in a minute.", null)));
                    return;
                }
                timestamps.addLast(now);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.endsWith("/auth/authenticate") || path.endsWith("/auth/register") || path.endsWith("/auth/refresh");
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        return ip + ":" + request.getRequestURI();
    }
}
