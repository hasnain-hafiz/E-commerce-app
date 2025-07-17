package com.Ecom.E_commerce.app.utils.dto;

import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.model.Product;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long id;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String productName;
}
