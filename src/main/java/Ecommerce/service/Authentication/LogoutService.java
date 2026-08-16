package Ecommerce.service.Authentication;

import Ecommerce.jwt.CookieUtil;
import Ecommerce.jwt.JwtService;
import Ecommerce.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compatibility logout handler for integrations that invoke a LogoutHandler
 * directly. The active controller-based logout flow delegates to
 * AuthenticationService, but this handler remains schema-compatible and
 * revokes hashed refresh tokens when it is used.
 */
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final TokenRepository tokenRepo;
    private final JwtService jwtService;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String refreshToken = CookieUtil.readCookie(request, CookieUtil.REFRESH_COOKIE);
        if (refreshToken == null || refreshToken.isBlank()) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return;
            }
            refreshToken = authHeader.substring(7);
        }

        tokenRepo.findByTokenHashAndRevokedFalse(jwtService.hashToken(refreshToken))
                .ifPresent(storedToken -> {
                    storedToken.setRevoked(true);
                    tokenRepo.save(storedToken);
                });
    }
}
