package com.Ecom.E_commerce.app.utils.request;

import com.Ecom.E_commerce.app.model.Category;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddProductRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Min(0)
    private BigDecimal price;
    @Min(0)
    private int inventory;
    @NotEmpty
    private String brand;
    @NotNull
    @Valid
    private Category category;
}
