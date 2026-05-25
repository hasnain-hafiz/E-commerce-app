package Ecommerce.service.Authentication;

import Ecommerce.jwt.JwtService;
import Ecommerce.model.Token;
import Ecommerce.model.user.CustomUserDetails;
import Ecommerce.model.user.User;
import Ecommerce.repository.TokenRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.dto.UserDto;
import Ecommerce.utils.enums.UserRole;
import Ecommerce.utils.exceptions.AlreadyExistsException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import Ecommerce.utils.request.AuthRequest;
import Ecommerce.utils.request.RegisterRequest;
import Ecommerce.utils.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static Ecommerce.utils.enums.UserRole.ROLE_CUSTOMER;
import static Ecommerce.utils.enums.UserRole.ROLE_SELLER;

@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepo;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        var e_user= userRepository.findByEmail(registerRequest.getEmail());
        if(e_user.isPresent()) { throw new AlreadyExistsException("Email already exists!");}
        System.out.println("role-seller=" +registerRequest.isSeller());

       var user = User.builder()
               .firstName(registerRequest.getFirstName())
               .lastName(registerRequest.getLastName())
               .email(registerRequest.getEmail())
               .password(passwordEncoder.encode(registerRequest.getPassword()))
               .roles(!registerRequest.isSeller() ? Set.of(ROLE_CUSTOMER) : Set.of(ROLE_SELLER))
               .build();
        System.out.println("user-role=" +user.getEmail() + user.getRoles());
        var savedUser = userRepository.save(user);
        var token = jwtService.generateToken(new CustomUserDetails(savedUser));

        revokeAllUserToken(savedUser);
        saveUserToken(savedUser, token);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);

        if(savedUser.getRoles().contains(ROLE_CUSTOMER)){
             authResponse.setSeller(false);
        }
        else{
            authResponse.setSeller(true);
        }

        return authResponse;
    }

    @Override
    @Transactional
    public AuthResponse authenticate(AuthRequest authRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                ));

        var user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        var token = jwtService.generateToken(new CustomUserDetails(user));

        revokeAllUserToken(user);
        saveUserToken(user, token);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
        if(user.getRoles().contains(ROLE_CUSTOMER)){
            authResponse.setSeller(false);
        }
        else{
            authResponse.setSeller(true);
        }

        return authResponse;
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

    @Override
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id" + id));
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User not found with id" + id)
        );
        userRepository.deleteById(id);
    }

    @Override
    public List<UserDto> convertAllUsersToDto(List<User> users){
        return users.stream().map(this::convertUserToDto).toList();
    }

    @Override
    public UserDto convertUserToDto(User user){
       return modelMapper.map(user, UserDto.class);
    }
}