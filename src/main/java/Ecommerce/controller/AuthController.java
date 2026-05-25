package Ecommerce.controller;

import Ecommerce.model.user.User;
import Ecommerce.service.Authentication.AuthenticationService;
import Ecommerce.utils.dto.UserDto;
import Ecommerce.utils.exceptions.AlreadyExistsException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import Ecommerce.utils.request.AuthRequest;
import Ecommerce.utils.request.RegisterRequest;
import Ecommerce.utils.response.ApiResponse;
import Ecommerce.utils.response.AuthResponse;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    @GetMapping("/warmup")
    public String warmup(){
        return "Server is up and running!";
    }

    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest){
        try {
            AuthResponse authResponse = authService.register(registerRequest);
            return ResponseEntity.ok(new ApiResponse("Signup successful!", authResponse));
        }
        catch (AlreadyExistsException e){
            return ResponseEntity.status(CONFLICT).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/authenticate")
    @PermitAll
    public ResponseEntity<ApiResponse> authenticate(@Valid @RequestBody AuthRequest authRequest){
       try {
           AuthResponse authResponse = authService.authenticate(authRequest);
           return ResponseEntity.ok(new ApiResponse("Login successful!", authResponse));
       }
       catch (Exception e){
           return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse("Invalid Email or Password", e.getMessage()));
       }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllUsers(){
        try {
            List<User> users = authService.getAllUsers();
            List<UserDto> userDtos = authService.convertAllUsersToDto(users);
            return ResponseEntity.ok(new ApiResponse("Users fetched successfully!", userDtos));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId){
        try {
            User user = authService.getUserById(userId);
            UserDto userDto = authService.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("User fetched successfully!",userDto));
        }
        catch (UserNotFoundException e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteUserById(@PathVariable Long userId){
        try {
            authService.deleteUserById(userId);
            return ResponseEntity.ok(new ApiResponse("User deleted successfully!", null));
        }
        catch (UserNotFoundException e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage()));
        }
    }
}