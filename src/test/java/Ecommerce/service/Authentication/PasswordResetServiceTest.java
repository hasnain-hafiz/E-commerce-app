package Ecommerce.service.Authentication;

import Ecommerce.model.PasswordResetToken;
import Ecommerce.model.Token;
import Ecommerce.model.user.User;
import Ecommerce.repository.PasswordResetTokenRepository;
import Ecommerce.repository.RefreshTokenRepository;
import Ecommerce.repository.TokenRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.exceptions.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IEmailService emailService;
    @Mock private TokenRepository tokenRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("buyer@shop.com");

        ReflectionTestUtils.setField(passwordResetService, "expirationMinutes", 30L);
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:5173");
    }

    @Test
    void requestReset_createsTokenAndSendsEmail_whenUserExists() {
        when(userRepository.findByEmail("buyer@shop.com")).thenReturn(Optional.of(user));

        passwordResetService.requestReset("buyer@shop.com");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("buyer@shop.com"), contains("reset-password?token="));
    }

    @Test
    void requestReset_doesNothingSilently_whenEmailDoesNotExist() {
        when(userRepository.findByEmail("nobody@shop.com")).thenReturn(Optional.empty());

        // Must not throw, and must not leak whether the email exists via
        // any side effect a caller could observe.
        assertDoesNotThrow(() -> passwordResetService.requestReset("nobody@shop.com"));
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void resetPassword_updatesPasswordAndMarksTokenUsed_whenValid() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newSecurePassword")).thenReturn("hashed");
        when(tokenRepository.findAllValidTokenByUser(1L)).thenReturn(Collections.emptyList());
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(Collections.emptyList());

        passwordResetService.resetPassword("valid-token", "newSecurePassword");

        assertEquals("hashed", user.getPassword());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).save(token);
    }

    @Test
    void resetPassword_revokesExistingSessions() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("valid-token");
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        Token activeAccessToken = new Token();
        activeAccessToken.setRevoked(false);
        activeAccessToken.setExpired(false);

        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(tokenRepository.findAllValidTokenByUser(1L)).thenReturn(List.of(activeAccessToken));
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(Collections.emptyList());

        passwordResetService.resetPassword("valid-token", "newSecurePassword");

        assertTrue(activeAccessToken.isRevoked());
        assertTrue(activeAccessToken.isExpired());
        verify(tokenRepository).saveAll(List.of(activeAccessToken));
    }

    @Test
    void resetPassword_throwsInvalidToken_whenAlreadyUsed() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("used-token");
        token.setUser(user);
        token.setUsed(true);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class,
                () -> passwordResetService.resetPassword("used-token", "newSecurePassword"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPassword_throwsInvalidToken_whenExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken("expired-token");
        token.setUser(user);
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(InvalidTokenException.class,
                () -> passwordResetService.resetPassword("expired-token", "newSecurePassword"));
        verify(userRepository, never()).save(any());
    }
}
