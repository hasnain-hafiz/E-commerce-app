package Ecommerce.utils.exceptions;

/**
 * Thrown for an invalid, expired, revoked, or already-used refresh token
 * or password reset token.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
