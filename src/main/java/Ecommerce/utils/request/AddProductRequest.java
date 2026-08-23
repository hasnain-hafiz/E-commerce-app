package Ecommerce.utils.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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

    // CHANGED: was `Category category` (the full JPA entity). The frontend
    // (AddProduct.jsx) has only ever sent a plain category name string —
    // this "worked" before only because Category happens to have a
    // single-arg String constructor Jackson could pick up implicitly,
    // which is fragile and not an intentional contract. Now explicit:
    // the client sends the category name, the service looks it up.
    @NotBlank
    private String category;
}
