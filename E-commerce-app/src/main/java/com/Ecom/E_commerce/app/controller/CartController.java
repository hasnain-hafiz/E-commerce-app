package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.dto.CartDto;
import com.Ecom.E_commerce.app.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.response.ApiResponse;
import com.Ecom.E_commerce.app.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;


@RestController
@RequestMapping("${api.prefix}/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/initialize")
    public ResponseEntity<ApiResponse> initializeCart(){
            Cart cart = cartService.initializeCart();
            return ResponseEntity.status(CREATED).body(new ApiResponse("Cart initialized successfully!", cart.getId()));
    }

    @GetMapping("/{cartId}")
    public ResponseEntity<ApiResponse> getCartById(@PathVariable Long cartId){
        try {
            Cart cart = cartService.getCart(cartId);
            CartDto cartDto = cartService.convertToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Cart fetched successfully!", cartDto));
        }
        catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse( "error" , e.getMessage()));
        }
    }

    @PostMapping("/{cartId}/product/{productId}")
    public ResponseEntity<ApiResponse> addItemToCart(@PathVariable Long productId, @PathVariable Long cartId){
        try{
            Cart cart = cartService.addItemToCart(cartId, productId);
            CartDto cartDto = cartService.convertToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Item added to cart successfully!", cartDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }

    @PutMapping("/{cartId}/product/{productId}")
    public ResponseEntity<ApiResponse> updateItemQuantity(@PathVariable Long productId, @PathVariable Long cartId, @RequestParam int quantity){
        try{
            Cart cart = cartService.updateItemQuantity( cartId, productId, quantity);
            CartDto cartDto = cartService.convertToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Item quantity updated successfully!", cartDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }

    @DeleteMapping("/{cartId}/product/{productId}")
    public ResponseEntity<ApiResponse> removeItemFromCart(@PathVariable Long productId, @PathVariable Long cartId){
        try{
            Cart cart = cartService.removeItemFromCart( cartId, productId);
            CartDto cartDto = cartService.convertToDto(cart);
            return ResponseEntity.ok(new ApiResponse("Item removed from cart successfully!", cartDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<ApiResponse> clearCart(@PathVariable Long cartId){
        try {
             cartService.clearCart(cartId);
             return ResponseEntity.ok(new ApiResponse("Cart cleared successfully!", null));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error" , e.getMessage()));
        }
    }
}
