package com.Ecom.E_commerce.app.dto;

import com.Ecom.E_commerce.app.model.CartItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class CartDto {
    private Long id;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private Set<CartItemDto> items;

}
