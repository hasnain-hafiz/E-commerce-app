package Ecommerce.model.user;

import Ecommerce.model.Cart;
import Ecommerce.model.Order;
import Ecommerce.model.Token;
import Ecommerce.utils.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles;

    @Builder.Default
    private int failedLoginAttempts = 0;

    private java.time.Instant lockedUntil;

    @Builder.Default
    private Boolean enabled = true;

    /**
     * Treat legacy NULL values as enabled so Hibernate can load existing users
     * created before this column was introduced as a primitive boolean.
     */
    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Order> orders;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Token> tokens;

}
