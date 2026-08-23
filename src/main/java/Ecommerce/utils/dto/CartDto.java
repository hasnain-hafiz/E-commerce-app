package Ecommerce.utils.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class CartDto {
    @NotNull
    private Long id;
    @Min(0)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    @Valid
    private List<CartItemDto> items;
}
