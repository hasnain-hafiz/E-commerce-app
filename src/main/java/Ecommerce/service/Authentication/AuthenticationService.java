package Ecommerce.service.Authentication;

import Ecommerce.jwt.JwtService;
import Ecommerce.model.Token;
import Ecommerce.model.user.CustomUserDetails;
import Ecommerce.model.user.User;
import Ecommerce.repository.TokenRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.dto.UserDto;
import Ecommerce.utils.enums.TokenType;
import Ecommerce.utils.enums.UserRole;
import Ecommerce.utils.exceptions.AlreadyExistsException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import Ecommerce.utils.request.AuthRequest;
import Ecommerce.utils.request.RegisterRequest;
import Ecommerce.utils.response.AuthTokens;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static Ecommerce.utils.enums.UserRole.ROLE_CUSTOMER;
import static Ecommerce.utils.enums.UserRole.ROLE_SELLER;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepo;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AuthTokens register(RegisterRequest registerRequest) {
        var existing = userRepository.findByEmail(registerRequest.getEmail());
        if (existing.isPresent()) {
            throw new AlreadyExistsException("Email already exists!");
        }

        var user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(!registerRequest.isSeller() ? Set.of(ROLE_CUSTOMER) : Set.of(ROLE_SELLER))
                .failedLoginAttempts(0)
                .enabled(true)
                .build();

        var savedUser = userRepository.save(user);
        return issueTokens(savedUser);
    }

    @Override
    @Transactional
    public AuthTokens authenticate(AuthRequest authRequest) {
        var user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new LockedException("Account temporarily locked due to repeated failed logins. Try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        } catch (BadCredentialsException e) {
            registerFailedAttempt(user);
            throw new BadCredentialsException("Invalid email or password");
        }

        // success - reset lockout counters
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return issueTokens(user);
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plusSeconds(LOCK_DURATION_MINUTES * 60));
        }
        userRepository.save(user);
    }

    @Transactional
    public AuthTokens refresh(String refreshToken) {
        if (refreshToken == null || !jwtService.isValidRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        String hash = jwtService.hashToken(refreshToken);
        Token stored = tokenRepo.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new BadCredentialsException("Refresh token not recognized or already used"));

        if (stored.isExpired()) {
            throw new BadCredentialsException("Refresh token expired");
        }

        // Rotate: revoke the one being used so it can never be replayed.
        stored.setRevoked(true);
        tokenRepo.save(stored);

        User user = stored.getUser();
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null) return;
        String hash = jwtService.hashToken(refreshToken);
        tokenRepo.findByTokenHashAndRevokedFalse(hash).ifPresent(t -> {
            t.setRevoked(true);
            tokenRepo.save(t);
        });
    }

    private AuthTokens issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Revoke any prior outstanding refresh tokens for this user (single active session model).
        tokenRepo.findAllValidTokensByUser(user.getId()).forEach(t -> t.setRevoked(true));

        Token tokenRecord = Token.builder()
                .user(user)
                .tokenHash(jwtService.hashToken(refreshToken))
                .tokenType(TokenType.REFRESH)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusMillis(jwtService.getRefreshTokenExpiryMs()))
                .build();
        tokenRepo.save(tokenRecord);

        Set<String> roleNames = user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet());
        return new AuthTokens(accessToken, refreshToken, user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), roleNames, jwtService.getAccessTokenExpiryMs(), jwtService.getRefreshTokenExpiryMs());
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> convertAllUsersToDto(List<User> users) {
        return users.stream().map(this::convertUserToDto).toList();
    }

    @Override
    public UserDto convertUserToDto(User user) {
        UserDto dto = modelMapper.map(user, UserDto.class);
        dto.setRoles(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
        return dto;
    }
}
