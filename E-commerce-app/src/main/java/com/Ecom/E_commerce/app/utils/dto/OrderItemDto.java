package com.Ecom.E_commerce.app.utils.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {

    private Long id;
    private int quantity;
    private String productName;
    private BigDecimal price;
}
