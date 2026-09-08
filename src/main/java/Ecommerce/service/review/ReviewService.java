package Ecommerce.service.review;

import Ecommerce.model.Product;
import Ecommerce.model.Review;
import Ecommerce.model.user.User;
import Ecommerce.repository.OrderRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.ReviewRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.dto.ReviewDto;
import Ecommerce.utils.enums.OrderStatus;
import Ecommerce.utils.enums.UserRole;
import Ecommerce.utils.exceptions.AlreadyExistsException;
import Ecommerce.utils.exceptions.ForbiddenException;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.exceptions.ReviewNotAllowedException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import Ecommerce.utils.request.AddReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService implements IReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<ReviewDto> getReviewsForProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::convertToDto).toList();
    }

    @Override
    @Transactional
    public ReviewDto addReview(Long productId, AddReviewRequest request) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // "Verified purchase" gate: only customers who have actually
        // ordered this product (in any non-cancelled order) may review it.
        // Deliberately not restricted to DELIVERED specifically — there's
        // no admin order-status-update flow yet (that's Phase 3+), so
        // requiring DELIVERED would make reviews impossible to demo
        // end-to-end today. Revisit once admin order management exists.
        boolean purchased = orderRepository.hasPurchased(user.getId(), productId, OrderStatus.CANCELLED);
        if (!purchased) {
            throw new ReviewNotAllowedException("You can only review products you've purchased");
        }

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new AlreadyExistsException("You've already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());

        return convertToDto(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        User currentUser = getCurrentUser();
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().contains(UserRole.ROLE_ADMIN);

        // Owner can delete their own review; ADMIN can moderate any review.
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to delete this review");
        }

        reviewRepository.delete(review);
    }

    private ReviewDto convertToDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct().getId());
        dto.setUserId(review.getUser().getId());
        dto.setReviewerName(review.getUser().getFirstName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
