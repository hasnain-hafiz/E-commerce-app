package Ecommerce.service.Authentication;

import Ecommerce.model.PasswordResetToken;
import Ecommerce.model.user.User;
import Ecommerce.repository.PasswordResetTokenRepository;
import Ecommerce.repository.RefreshTokenRepository;
import Ecommerce.repository.TokenRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.exceptions.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IEmailService emailService;
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${password-reset.token-expiration-minutes:30}")
    private long expirationMinutes;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Deliberately does NOT reveal whether the email exists — the caller
     * always sees the same generic success response (see AuthController),
     * to avoid using this endpoint to enumerate registered emails. The
     * token/email are only actually created/"sent" if the account exists.
     */
    @Transactional
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(UUID.randomUUID().toString());
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken.getToken();
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset link"));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("This reset link has already been used");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("This reset link has expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Changing the password invalidates all existing sessions — both
        // the JWT allowlist and any outstanding refresh tokens — standard
        // practice for a password-reset flow (e.g. the reset may be a
        // recovery from a compromised account).
        List<Ecommerce.model.Token> validTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        validTokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });
        tokenRepository.saveAll(validTokens);

        List<Ecommerce.model.RefreshToken> validRefreshTokens =
                refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        validRefreshTokens.forEach(rt -> rt.setRevoked(true));
        refreshTokenRepository.saveAll(validRefreshTokens);
    }
}
