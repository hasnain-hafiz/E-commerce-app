package com.Ecom.E_commerce.app.request;

import com.Ecom.E_commerce.app.model.Category;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int inventory;
    private String brand;
    private Category category;
}
