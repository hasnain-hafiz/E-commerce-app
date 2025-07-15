package com.Ecom.E_commerce.app.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class OrderDto {
    private Long id;
    private Long userId;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    private Set<OrderItemDto> items;
}
