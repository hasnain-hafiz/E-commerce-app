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

import java.util.HashSet;
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

    // CHANGED: previously annotated with only @Enumerated and no
    // collection-mapping annotation — a bare @Enumerated on a collection
    // field is not a valid JPA mapping (JPA requires @ElementCollection
    // for a collection of basic/enum values). Made explicit so
    // ddl-auto=validate has something well-defined to check, and so the
    // baseline Flyway migration can commit to an actual table shape
    // (user_roles) instead of guessing.
    // IMPORTANT: verify this matches how roles are actually stored in the
    // existing Neon database before relying on ddl-auto=validate there —
    // see the note in V1__baseline.sql.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<UserRole> roles = new HashSet<>();

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private Cart cart;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Order> orders;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<Token> tokens;

}
