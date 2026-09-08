package Ecommerce.model;

import Ecommerce.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 255, nullable = false)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiresAt;

    private boolean used;
}
