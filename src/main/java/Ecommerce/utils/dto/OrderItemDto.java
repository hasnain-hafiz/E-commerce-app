package Ecommerce.utils.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {

    @NotNull
    private Long id;
    @Min(1)
    private int quantity;
    @NotBlank
    private String productName;
    @NotBlank
    private Long productId;
    @NotNull
    private BigDecimal price;
}
