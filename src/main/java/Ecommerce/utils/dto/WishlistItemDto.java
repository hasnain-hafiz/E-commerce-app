package Ecommerce.utils.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WishlistItemDto {
    private Long id;
    private ProductDto product;
    private LocalDateTime addedAt;
}
