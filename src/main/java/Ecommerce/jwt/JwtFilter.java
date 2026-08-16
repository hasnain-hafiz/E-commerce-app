package Ecommerce.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the short-lived access-token JWT. Reads it from the httpOnly
 * "access_token" cookie (the standard path for the SPA); falls back to the
 * Authorization header for non-browser API clients. No DB lookup on the hot
 * path - the token's signature + embedded roles/expiry are trusted directly,
 * which is safe because access tokens are short-lived and revocation only
 * needs to work for refresh tokens.
 */
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = CookieUtil.readCookie(request, CookieUtil.ACCESS_COOKIE);

        if (token == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                token = header.substring(7);
            }
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtService.isValidAccessToken(token)) {
                String email = jwtService.extractSubject(token);
                List<String> roles = jwtService.extractRoles(token);
                List<GrantedAuthority> authorities = roles == null ? List.of() :
                        roles.stream().map(SimpleGrantedAuthority::new).map(GrantedAuthority.class::cast).toList();

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(email, null, authorities);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
