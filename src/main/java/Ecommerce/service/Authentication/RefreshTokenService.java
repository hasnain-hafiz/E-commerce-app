package Ecommerce.service.Authentication;

import Ecommerce.model.RefreshToken;
import Ecommerce.model.user.User;
import Ecommerce.repository.RefreshTokenRepository;
import Ecommerce.utils.exceptions.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays));
        refreshToken.setRevoked(false);
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Validates the given refresh token and rotates it: the old token is
     * revoked and a brand new one is issued and returned. Rotation limits
     * the blast radius of a leaked refresh token — once it's used once (by
     * its legitimate owner or an attacker), the old value stops working.
     * NOTE: a genuinely hardened implementation would treat "someone tried
     * to reuse an already-revoked token" as a signal of possible theft and
     * revoke the whole token family / force re-login. Not wired up here —
     * flagged in PHASE_3_SUMMARY.md as a real follow-up, not silently
     * skipped.
     */
    @Transactional
    public RefreshToken verifyAndRotate(String tokenValue) {
        RefreshToken existing = refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (existing.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }
        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return createRefreshToken(existing.getUser());
    }

    @Transactional
    public void revoke(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }
}
