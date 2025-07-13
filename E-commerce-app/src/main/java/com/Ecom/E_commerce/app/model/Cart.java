package com.Ecom.E_commerce.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "cart",cascade = CascadeType.ALL,orphanRemoval = true)
    private Set<CartItem> items;

    public void addItem(CartItem item) {
        this.items.add(item);
        item.setCart(this);
        recalculateTotal();
    }

    public void removeItem(CartItem item) {
        this.items.remove(item);
        item.setCart(null);
        recalculateTotal();
    }

    public void recalculateTotal() {
        updateTotalAmount();
    }

    private void updateTotalAmount() {
        this.totalAmount = items.stream().map(item -> {
            BigDecimal unitPrice = item.getUnitPrice();
            if (unitPrice == null) {
                return  BigDecimal.ZERO;
            }
            return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
