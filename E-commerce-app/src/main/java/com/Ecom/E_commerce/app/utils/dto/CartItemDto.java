package com.Ecom.E_commerce.app.utils.dto;

import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.model.Product;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDto {
    @NotNull
    private Long id;
    @Min(0)
    private int quantity;
    @Min(0)
    private BigDecimal unitPrice;
    @Min(0)
    private BigDecimal totalPrice;
    @NotBlank
    private String productName;
}
