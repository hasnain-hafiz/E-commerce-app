package com.Ecom.E_commerce.app.utils.dto;

import com.Ecom.E_commerce.app.model.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDto {
    @NotNull
    private Long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @Min(0)
    private BigDecimal price;
    @Min(0)
    private int inventory;
    @NotBlank
    private String brand;
    @NotNull
    @Valid
    private Category category;
    private List<ImageDto> imageList;
}
