package Ecommerce.utils.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long id;
    private Long productId;
    private Long userId;
    private String reviewerName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
