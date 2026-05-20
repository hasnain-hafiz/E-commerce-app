package Ecommerce.utils.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {
    @NotNull
    private Long id;
    @NotNull
    private Long productId;
    @Min(0)
    private int quantity;
    @Min(0)
    private BigDecimal unitPrice;
    @Min(0)
    private BigDecimal totalPrice;
    @NotBlank
    private String productName;
}
