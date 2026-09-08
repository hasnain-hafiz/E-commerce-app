package Ecommerce.utils.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    @NotBlank
    private String token;

    // NEW (Phase 3): opaque refresh token, exchanged via POST /auth/refresh
    // for a new short-lived access token without re-entering credentials.
    private String refreshToken;

    private boolean seller;
}
