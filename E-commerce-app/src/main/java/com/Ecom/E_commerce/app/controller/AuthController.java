package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.model.user.User;
import com.Ecom.E_commerce.app.utils.exceptions.AlreadyExistsException;
import com.Ecom.E_commerce.app.utils.exceptions.UserNotFoundException;
import com.Ecom.E_commerce.app.utils.request.AuthRequest;
import com.Ecom.E_commerce.app.utils.request.RegisterRequest;
import com.Ecom.E_commerce.app.utils.response.ApiResponse;
import com.Ecom.E_commerce.app.service.Authentication.AuthenticationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipalNotFoundException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    @PostMapping("/register")
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
    public ResponseEntity<ApiResponse> getAllUsers(){
        try {
            return ResponseEntity.ok(new ApiResponse("Users fetched successfully!", authService.getAllUsers()));
        }
        catch (Exception e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long userId){
        try {
            User user = authService.getUserById(userId);
            return ResponseEntity.ok(new ApiResponse("User fetched successfully!",user));
        }
        catch (UserNotFoundException e){
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("error",e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}")
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