package com.Ecom.E_commerce.app.service.Authentication;

import com.Ecom.E_commerce.app.model.user.CustomUserDetails;
import com.Ecom.E_commerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.Ecom.E_commerce.app.model.user.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {

        User user = userRepo.findByEmail(login).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetails(user);
    }
}
