package com.Ecom.E_commerce.app.service.Authentication;

import com.Ecom.E_commerce.app.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepo;

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");

        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            return;
        }

        String token= authHeader.substring(7);
        var storedToken = tokenRepo.findByToken(token).orElse(null);

        if(storedToken!=null){
            storedToken.setRevoked(true);
            storedToken.setExpired(true);
            tokenRepo.save(storedToken);
        }
    }
}
