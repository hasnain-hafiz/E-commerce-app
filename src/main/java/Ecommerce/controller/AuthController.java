package Ecommerce.controller;

import Ecommerce.jwt.CookieUtil;
import Ecommerce.model.user.User;
import Ecommerce.service.Authentication.AuthenticationService;
import Ecommerce.utils.dto.UserDto;
import Ecommerce.utils.request.AuthRequest;
import Ecommerce.utils.request.RegisterRequest;
import Ecommerce.utils.response.ApiResponse;
import Ecommerce.utils.response.AuthResponse;
import Ecommerce.utils.response.AuthTokens;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;
    private final CookieUtil cookieUtil;

    @GetMapping("/warmup")
    public String warmup() {
        return "Server is up and running!";
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthTokens tokens = authService.register(registerRequest);
        return withAuthCookies(tokens, "Signup successful!");
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<AuthResponse>> authenticate(@Valid @RequestBody AuthRequest authRequest) {
        AuthTokens tokens = authService.authenticate(authRequest);
        return withAuthCookies(tokens, "Login successful!");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(HttpServletRequest request) {
        String refreshToken = CookieUtil.readCookie(request, CookieUtil.REFRESH_COOKIE);
        AuthTokens tokens = authService.refresh(refreshToken);
        return withAuthCookies(tokens, "Session refreshed!");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String refreshToken = CookieUtil.readCookie(request, CookieUtil.REFRESH_COOKIE);
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieUtil.clearAccessCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieUtil.clearRefreshCookie().toString())
                .body(new ApiResponse<>("Logged out successfully!", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthResponse>> me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = authService.getCurrentUser(email);
        AuthResponse body = new AuthResponse(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()));
        return ResponseEntity.ok(new ApiResponse<>("Current user", body));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        List<User> users = authService.getAllUsers();
        List<UserDto> userDtos = authService.convertAllUsersToDto(users);
        return ResponseEntity.ok(new ApiResponse<>("Users fetched successfully!", userDtos));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long userId) {
        User user = authService.getUserById(userId);
        UserDto userDto = authService.convertUserToDto(user);
        return ResponseEntity.ok(new ApiResponse<>("User fetched successfully!", userDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUserById(@PathVariable Long userId) {
        authService.deleteUserById(userId);
        return ResponseEntity.ok(new ApiResponse<>("User deleted successfully!", null));
    }

    private ResponseEntity<ApiResponse<AuthResponse>> withAuthCookies(AuthTokens tokens, String message) {
        var accessCookie = cookieUtil.buildAccessCookie(tokens.accessToken(), tokens.accessTokenExpiryMs());
        var refreshCookie = cookieUtil.buildRefreshCookie(tokens.refreshToken(), tokens.refreshTokenExpiryMs());

        AuthResponse body = new AuthResponse(tokens.userId(), tokens.firstName(), tokens.lastName(), tokens.email(), tokens.roles());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(new ApiResponse<>(message, body));
    }
}
