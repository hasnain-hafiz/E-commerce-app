package Ecommerce.model;

import Ecommerce.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh tokens are opaque random strings (not JWTs) stored server-side
 * so they can be individually revoked/rotated -- unlike the short-lived
 * JWT access token, which is stateless and can't be "un-issued" before it
 * naturally expires.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 512, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiresAt;

    private boolean revoked;
}
