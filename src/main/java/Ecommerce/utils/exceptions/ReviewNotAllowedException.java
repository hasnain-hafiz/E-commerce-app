package Ecommerce.utils.exceptions;

/**
 * Thrown when a user tries to review a product they haven't purchased,
 * or tries to submit a second review for a product they've already reviewed.
 */
public class ReviewNotAllowedException extends RuntimeException {
    public ReviewNotAllowedException(String message) {
        super(message);
    }
}
