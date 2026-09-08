package Ecommerce.service.review;

import Ecommerce.model.Product;
import Ecommerce.model.Review;
import Ecommerce.model.user.User;
import Ecommerce.repository.OrderRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.ReviewRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.enums.OrderStatus;
import Ecommerce.utils.enums.UserRole;
import Ecommerce.utils.exceptions.AlreadyExistsException;
import Ecommerce.utils.exceptions.ForbiddenException;
import Ecommerce.utils.exceptions.ReviewNotAllowedException;
import Ecommerce.utils.request.AddReviewRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User buyer;
    private Product product;

    @BeforeEach
    void setUp() {
        buyer = new User();
        buyer.setId(1L);
        buyer.setEmail("buyer@shop.com");
        buyer.setFirstName("Asha");
        buyer.setRoles(Set.of(UserRole.ROLE_CUSTOMER));

        product = new Product();
        product.setId(50L);
        product.setName("Desk Lamp");

        when(userRepository.findByEmail("buyer@shop.com")).thenReturn(Optional.of(buyer));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer@shop.com", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AddReviewRequest request(int rating, String comment) {
        AddReviewRequest req = new AddReviewRequest();
        req.setRating(rating);
        req.setComment(comment);
        return req;
    }

    @Test
    void addReview_throwsReviewNotAllowed_whenUserNeverPurchasedTheProduct() {
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(orderRepository.hasPurchased(1L, 50L, OrderStatus.CANCELLED)).thenReturn(false);

        assertThrows(ReviewNotAllowedException.class,
                () -> reviewService.addReview(50L, request(5, "Great lamp")));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_throwsAlreadyExists_whenUserAlreadyReviewedTheProduct() {
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(orderRepository.hasPurchased(1L, 50L, OrderStatus.CANCELLED)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndProductId(1L, 50L)).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> reviewService.addReview(50L, request(4, "Still good")));
        verify(reviewRepository, never()).save(any());
    }

    @Test
    void addReview_succeeds_whenPurchasedAndNotAlreadyReviewed() {
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(orderRepository.hasPurchased(1L, 50L, OrderStatus.CANCELLED)).thenReturn(true);
        when(reviewRepository.existsByUserIdAndProductId(1L, 50L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        var dto = reviewService.addReview(50L, request(5, "Great lamp"));

        assertEquals(5, dto.getRating());
        assertEquals("Asha", dto.getReviewerName());
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    void deleteReview_throwsForbidden_whenCallerIsNeitherOwnerNorAdmin() {
        User someoneElse = new User();
        someoneElse.setId(2L);

        Review review = new Review();
        review.setId(10L);
        review.setUser(someoneElse);
        review.setProduct(product);

        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        assertThrows(ForbiddenException.class, () -> reviewService.deleteReview(10L));
        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void deleteReview_succeeds_whenCallerIsTheOwner() {
        Review review = new Review();
        review.setId(10L);
        review.setUser(buyer);
        review.setProduct(product);

        when(reviewRepository.findById(10L)).thenReturn(Optional.of(review));

        assertDoesNotThrow(() -> reviewService.deleteReview(10L));
        verify(reviewRepository).delete(review);
    }
}
