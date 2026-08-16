package Ecommerce.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import Ecommerce.model.user.User;
import Ecommerce.utils.enums.TokenType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Tracks issued REFRESH tokens only (access tokens are short-lived, stateless
 * JWTs validated purely by signature+expiry, so they never touch the DB).
 * The refresh token value itself is never stored in plaintext - only a
 * SHA-256 hash - so a DB leak alone cannot be used to forge a session.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tokens", indexes = {
        @Index(name = "idx_token_hash", columnList = "tokenHash", unique = true)
})
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private boolean revoked;

    private Instant expiresAt;

    private Instant createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public boolean isExpired() {
        return expiresAt == null || expiresAt.isBefore(Instant.now());
    }
}
