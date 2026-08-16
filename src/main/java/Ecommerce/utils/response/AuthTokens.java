package Ecommerce.utils.response;

import java.util.Set;

/** Internal carrier for a freshly issued token pair - never serialized to JSON directly. */
public record AuthTokens(String accessToken, String refreshToken, Long userId, String firstName, String lastName,
                          String email, Set<String> roles, long accessTokenExpiryMs, long refreshTokenExpiryMs) {
}
