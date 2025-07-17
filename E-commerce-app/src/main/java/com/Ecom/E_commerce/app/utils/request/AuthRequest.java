package com.Ecom.E_commerce.app.utils.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @NotBlank
    @Email
    private String email;
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
