package Ecommerce.utils.exceptions;

/**
 * Thrown when an authenticated user is valid and has the right role,
 * but does not own/is not permitted to act on the specific resource
 * they're targeting (e.g. a SELLER editing another seller's product).
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
