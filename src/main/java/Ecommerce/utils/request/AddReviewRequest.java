package Ecommerce.utils.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddReviewRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    // Optional — a star rating alone is a valid review.
    private String comment;
}
