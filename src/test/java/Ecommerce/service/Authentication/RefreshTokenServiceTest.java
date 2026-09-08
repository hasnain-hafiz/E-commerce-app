package Ecommerce.service.Authentication;

import Ecommerce.model.RefreshToken;
import Ecommerce.model.user.User;
import Ecommerce.repository.RefreshTokenRepository;
import Ecommerce.utils.exceptions.InvalidTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenExpirationDays", 7L);
        lenient().when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void verifyAndRotate_returnsNewToken_andRevokesTheOldOne() {
        RefreshToken existing = new RefreshToken();
        existing.setToken("old-token");
        existing.setUser(user);
        existing.setRevoked(false);
        existing.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByToken("old-token")).thenReturn(Optional.of(existing));

        RefreshToken rotated = refreshTokenService.verifyAndRotate("old-token");

        assertNotEquals("old-token", rotated.getToken());
        assertTrue(existing.isRevoked());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        assertTrue(captor.getAllValues().stream().anyMatch(RefreshToken::isRevoked));
    }

    @Test
    void verifyAndRotate_throwsInvalidToken_whenAlreadyRevoked() {
        RefreshToken revoked = new RefreshToken();
        revoked.setToken("used-token");
        revoked.setUser(user);
        revoked.setRevoked(true);
        revoked.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(refreshTokenRepository.findByToken("used-token")).thenReturn(Optional.of(revoked));

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.verifyAndRotate("used-token"));
    }

    @Test
    void verifyAndRotate_throwsInvalidToken_whenExpired() {
        RefreshToken expired = new RefreshToken();
        expired.setToken("expired-token");
        expired.setUser(user);
        expired.setRevoked(false);
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expired));

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.verifyAndRotate("expired-token"));
    }

    @Test
    void verifyAndRotate_throwsInvalidToken_whenTokenDoesNotExist() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidTokenException.class, () -> refreshTokenService.verifyAndRotate("unknown"));
    }
}
