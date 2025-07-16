package com.Ecom.E_commerce.app.service.Authentication;

import com.Ecom.E_commerce.app.utils.exceptions.AlreadyExistsException;
import com.Ecom.E_commerce.app.jwt.JwtService;
import com.Ecom.E_commerce.app.model.Token;
import com.Ecom.E_commerce.app.model.user.CustomUserDetails;
import com.Ecom.E_commerce.app.repository.TokenRepository;
import com.Ecom.E_commerce.app.repository.UserRepository;
import com.Ecom.E_commerce.app.utils.exceptions.UserNotFoundException;
import com.Ecom.E_commerce.app.utils.request.AuthRequest;
import com.Ecom.E_commerce.app.utils.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import com.Ecom.E_commerce.app.model.user.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepo;

    @Override
    public String register(RegisterRequest registerRequest) {
        var e_user= userRepository.findByEmail(registerRequest.getEmail());
        if(e_user.isPresent()) { throw new AlreadyExistsException("Email already exists!");}

       var user = User.builder()
               .firstName(registerRequest.getFirstName())
               .lastName(registerRequest.getLastName())
               .email(registerRequest.getEmail())
               .password(passwordEncoder.encode(registerRequest.getPassword()))
               .role(registerRequest.getRole())
               .build();

        var savedUser = userRepository.save(user);
        var token = jwtService.generateToken(new CustomUserDetails(savedUser));

        revokeAllUserToken(savedUser);
        saveUserToken(savedUser, token);

        return token;
    }

    @Override
    public String authenticate(AuthRequest authRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                ));

        var user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with Email!" + authRequest.getEmail()));
        var token = jwtService.generateToken(new CustomUserDetails(user));

        revokeAllUserToken(user);
        saveUserToken(user, token);

        return token;
    }

    private void revokeAllUserToken(User user){
        var allValidTokens = tokenRepo.findAllValidTokenByUser(user.getId());
        if(allValidTokens.isEmpty()){
            return;
        }
        allValidTokens.forEach((token ->{
            token.setRevoked(true);
            token.setExpired(true);
        }));
        tokenRepo.saveAll(allValidTokens);
    }

    private void saveUserToken(User savedUser, String jwtToken) {
        var token = Token.builder()
                .user(savedUser)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepo.save(token);
    }

    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id" + id));
    }

    public void deleteUserById(Long id) {
        userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found with id" + id)
        );
        userRepository.deleteById(id);
    }


}