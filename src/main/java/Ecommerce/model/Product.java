package Ecommerce.model;

import Ecommerce.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int inventory;
    private String brand;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Image> imageList;

    @ManyToOne
    @JoinColumn(name="seller_id")
    private User seller;

    public Product(String name, String description, String brand, BigDecimal price, int inventory, Category category, User seller) {
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.inventory = inventory;
        this.category = category;
        this.seller = seller;
    }
}
