package Ecommerce.utils.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * No raw token in the body - tokens travel only as httpOnly cookies.
 * This just tells the frontend who is logged in and what they can do.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Set<String> roles;
}
