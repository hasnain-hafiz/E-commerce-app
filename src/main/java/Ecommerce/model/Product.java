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

    // NEW: optimistic locking. Without this, two near-simultaneous checkouts
    // on the last unit(s) of a product could both read the same inventory
    // value, both pass the "enough stock" check, and both decrement it —
    // overselling. With @Version, the second concurrent write to the same
    // row fails with an ObjectOptimisticLockingFailureException instead of
    // silently succeeding; OrderService now catches that and surfaces a
    // clear "try again" error instead of corrupting inventory.
    // This does NOT fully solve high-contention flash-sale-scale races
    // (that would need pessimistic locking or a queue) — it's the right
    // tradeoff for this app's actual scale, and is documented as such.
    @Version
    private Long version;

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
