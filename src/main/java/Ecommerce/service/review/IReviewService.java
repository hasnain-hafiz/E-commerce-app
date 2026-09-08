package Ecommerce.service.review;

import Ecommerce.utils.dto.ReviewDto;
import Ecommerce.utils.request.AddReviewRequest;

import java.util.List;

public interface IReviewService {
    List<ReviewDto> getReviewsForProduct(Long productId);
    ReviewDto addReview(Long productId, AddReviewRequest request);
    void deleteReview(Long reviewId);
}
