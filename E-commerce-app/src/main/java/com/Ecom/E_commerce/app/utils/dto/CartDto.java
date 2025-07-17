package com.Ecom.E_commerce.app.utils.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class CartDto {
    @NotNull
    private Long id;
    @Min(0)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    @Valid
    private Set<CartItemDto> items;
}
