package Ecommerce.exception;

import Ecommerce.utils.exceptions.AlreadyExistsException;
import Ecommerce.utils.exceptions.ForbiddenException;
import Ecommerce.utils.exceptions.ProductNotFoundException;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.exceptions.ReviewNotAllowedException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import Ecommerce.utils.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

/**
 * Central place for turning exceptions into the standardized ApiError shape.
 *
 * NOTE (Phase 1 scope): most existing controllers still have their own
 * try/catch blocks that intercept ResourceNotFoundException etc. before
 * they reach here. This handler currently catches:
 *   - anything those per-controller catches don't cover (e.g. the new
 *     ForbiddenException thrown by ownership checks),
 *   - Bean Validation failures,
 *   - anything genuinely unexpected (falls back to a safe generic 500).
 * Removing the per-controller try/catch blocks in favor of always relying
 * on this handler is planned for a later phase so it can be done
 * consistently across all controllers at once, rather than piecemeal.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req, null);
    }

    @ExceptionHandler({UserNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<ApiError> handleDomainNotFound(RuntimeException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req, null);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiError> handleAlreadyExists(AlreadyExistsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "ALREADY_EXISTS", ex.getMessage(), req, null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiError> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), req, null);
    }

    // NEW (Phase 2b): "you haven't purchased this" / "you already reviewed
    // this" — a permission-shaped failure, so 403 rather than 400/409.
    @ExceptionHandler(ReviewNotAllowedException.class)
    public ResponseEntity<ApiError> handleReviewNotAllowed(ReviewNotAllowedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "REVIEW_NOT_ALLOWED", ex.getMessage(), req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to perform this action", req, null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
        // CHANGED: never echo the underlying auth exception message to the client.
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password", req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", req, errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req, null);
    }

    // NEW (Phase 2): thrown when Product's @Version check detects a
    // concurrent modification during checkout (e.g. two customers racing
    // to buy the last unit of the same product). 409 tells the client this
    // is a "your request conflicted, retry" situation, not a hard failure.
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "CONFLICT",
                "Some items in your cart were just updated by another purchase. Please review your cart and try again.",
                req, null);
    }

    // CHANGED: fallback for anything unexpected. Previously several
    // controllers returned e.getMessage() straight to the client for
    // generic Exception, which can leak internal details (e.g. raw DB
    // constraint messages). This always returns a safe generic message;
    // the real exception should still be logged server-side.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest req) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Something went wrong. Please try again later.", req, null);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message,
                                            HttpServletRequest req, List<String> errors) {
        ApiError body = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .code(code)
                .message(message)
                .path(req.getRequestURI())
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
