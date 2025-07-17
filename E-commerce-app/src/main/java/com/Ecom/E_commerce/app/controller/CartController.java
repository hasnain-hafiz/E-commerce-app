package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.model.user.CustomUserDetails;
import com.Ecom.E_commerce.app.repository.UserRepository;
import com.Ecom.E_commerce.app.utils.dto.CartDto;
import com.Ecom.E_commerce.app.utils.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.utils.response.ApiResponse;
import com.Ecom.E_commerce.app.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@RestController
@RequestMapping("${api.prefix}/cart")
@RequiredArgsConstructor
public class CartController {
      private final CartService cartService;
      private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email))
                .getId();
    }

    @GetMapping("/{cartId}/by-user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getCartByUserId(@PathVariable Long userId, @PathVariable Long cartId){
        try {
            Cart cart = cartService.getCart(cartId,userId);
            CartDto cartDto = cartService.convertCartToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Cart fetched successfully!", cartDto));
        }
        catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse( "error" , e.getMessage()));
        }
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> initializeCart(){
            Cart cart = cartService.initializeCart(getCurrentUserId());
            return ResponseEntity.status(CREATED).body(new ApiResponse("Cart initialized successfully!", cart.getId()));
    }

    @GetMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getCartById(@PathVariable Long cartId){
        try {
            Cart cart = cartService.getCart(cartId,getCurrentUserId());
            CartDto cartDto = cartService.convertCartToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Cart fetched successfully!", cartDto));
        }
        catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse( "error" , e.getMessage()));
        }
    }

    @PostMapping("/{cartId}/product/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> addItemToCart(@PathVariable Long productId, @PathVariable Long cartId){
        try{
            Cart cart = cartService.addItemToCart(cartId, productId, getCurrentUserId());
            CartDto cartDto = cartService.convertCartToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Item added to cart successfully!", cartDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }

    @PutMapping("/{cartId}/product/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable Long productId, @PathVariable Long cartId, @RequestParam int quantity){
        try{
            Cart cart = cartService.updateItemQuantity( cartId, productId, quantity, getCurrentUserId());
            CartDto cartDto = cartService.convertCartToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Item quantity updated successfully!", cartDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }

    @DeleteMapping("/{cartId}/product/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long productId, @PathVariable Long cartId){
        try{
            Cart cart = cartService.removeItemFromCart( cartId, productId , getCurrentUserId());
            CartDto cartDto = cartService.convertCartToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Item removed from cart successfully!", cartDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long cartId){
        try {
             cartService.clearCart(cartId , getCurrentUserId());
             return ResponseEntity.ok(new ApiResponse("Cart cleared successfully!", null));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }
}
