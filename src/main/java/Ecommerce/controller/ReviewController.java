package Ecommerce.controller;

import Ecommerce.service.review.IReviewService;
import Ecommerce.utils.dto.ReviewDto;
import Ecommerce.utils.request.AddReviewRequest;
import Ecommerce.utils.response.ApiResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/review")
@RequiredArgsConstructor
public class ReviewController {

    private final IReviewService reviewService;

    @GetMapping("/product/{productId}")
    @PermitAll
    public ResponseEntity<ApiResponse> getReviewsForProduct(@PathVariable Long productId) {
        List<ReviewDto> reviews = reviewService.getReviewsForProduct(productId);
        return ResponseEntity.ok(new ApiResponse("Reviews fetched successfully!", reviews));
    }

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> addReview(@PathVariable Long productId,
                                                  @Valid @RequestBody AddReviewRequest request) {
        ReviewDto review = reviewService.addReview(productId, request);
        return ResponseEntity.ok(new ApiResponse("Review added successfully!", review));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(new ApiResponse("Review deleted successfully!", null));
    }
}
