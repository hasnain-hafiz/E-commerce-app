package com.Ecom.E_commerce.app.utils.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
    @NotNull
    private BigDecimal price;
}
