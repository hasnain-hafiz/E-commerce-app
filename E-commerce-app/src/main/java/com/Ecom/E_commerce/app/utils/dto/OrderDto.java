package com.Ecom.E_commerce.app.utils.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class OrderDto {
    @NotNull
    private Long id;
    @NotNull
    private Long userId;
    private LocalDateTime orderDate;
    @Min(0)
    private BigDecimal totalAmount;
    @NotBlank
    private String status;
    @NotEmpty
    @Valid
    private Set<OrderItemDto> items;
}
