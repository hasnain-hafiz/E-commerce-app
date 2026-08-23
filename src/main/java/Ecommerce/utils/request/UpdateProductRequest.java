package Ecommerce.utils.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateProductRequest {

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

    // CHANGED: was `Category category` — see AddProductRequest for why.
    @NotBlank
    private String category;
}
