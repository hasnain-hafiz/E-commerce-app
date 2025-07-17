package com.Ecom.E_commerce.app.utils.helper;

import com.Ecom.E_commerce.app.model.user.CustomUserDetails;
import com.Ecom.E_commerce.app.model.user.User;
import com.Ecom.E_commerce.app.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserOrThrow {
    private final UserRepository userRepository;

    public AuthenticatedUserOrThrow(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUserOrThrow() {
        CustomUserDetails currentUser = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
