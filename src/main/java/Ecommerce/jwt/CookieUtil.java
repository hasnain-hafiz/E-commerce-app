package Ecommerce.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";

    @Value("${app.cookie.secure:true}")
    private boolean secure;

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    public ResponseCookie buildAccessCookie(String token, long maxAgeMs) {
        return build(ACCESS_COOKIE, token, maxAgeMs / 1000, "/");
    }

    public ResponseCookie buildRefreshCookie(String token, long maxAgeMs) {
        // Scoped to the auth path only - the browser will not send it on every request,
        // shrinking the blast radius if a script running on another route is compromised.
        return build(REFRESH_COOKIE, token, maxAgeMs / 1000, "/api/v1/auth");
    }

    public ResponseCookie clearAccessCookie() {
        return build(ACCESS_COOKIE, "", 0, "/");
    }

    public ResponseCookie clearRefreshCookie() {
        return build(REFRESH_COOKIE, "", 0, "/api/v1/auth");
    }

    private ResponseCookie build(String name, String value, long maxAgeSeconds, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAgeSeconds)
                .build();
    }

    public static String readCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (var c : request.getCookies()) {
            if (c.getName().equals(name)) return c.getValue();
        }
        return null;
    }
}
