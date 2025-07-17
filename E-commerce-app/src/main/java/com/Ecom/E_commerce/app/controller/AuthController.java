package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.model.user.User;
import com.Ecom.E_commerce.app.utils.dto.UserDto;
import com.Ecom.E_commerce.app.utils.exceptions.AlreadyExistsException;
import com.Ecom.E_commerce.app.utils.exceptions.UserNotFoundException;
import com.Ecom.E_commerce.app.utils.request.AuthRequest;
import com.Ecom.E_commerce.app.utils.request.RegisterRequest;
import com.Ecom.E_commerce.app.utils.response.ApiResponse;
import com.Ecom.E_commerce.app.service.Authentication.AuthenticationService;
import jakarta.annotation.security.PermitAll;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    @PermitAll
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest registerRequest){
        try {
            String token = authService.register(registerRequest);
            return ResponseEntity.ok(new ApiResponse("User registered successfully!", token));
        }
        catch (AlreadyExistsException e){
            return ResponseEntity.status(CONFLICT).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PostMapping("/authenticate")
    @PermitAll
    public ResponseEntity<ApiResponse> authenticate(@RequestBody AuthRequest authRequest){
       try {
           String token = authService.authenticate(authRequest);
           return ResponseEntity.ok(new ApiResponse("User authenticated successfully!", token));
       }
       catch (Exception e){
           return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse("error", e.getMessage()));
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