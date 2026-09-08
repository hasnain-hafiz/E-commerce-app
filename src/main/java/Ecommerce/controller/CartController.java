package Ecommerce.controller;

import Ecommerce.model.Cart;
import Ecommerce.service.cart.CartService;
import Ecommerce.utils.dto.CartDto;
import Ecommerce.utils.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// CHANGED (Phase 3): removed @CrossOrigin(origins = "...") — CORS is
// handled once, globally, by SecurityConfig.corsConfigurationSource().
@RestController
@RequestMapping("${api.prefix}/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getCart() {
        Cart cart = cartService.getCart();
        CartDto cartDto = cartService.convertCartToDto(cart);

        return ResponseEntity.ok(
                new ApiResponse("Cart fetched successfully!", cartDto)
        );
    }

    @PostMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> addItem(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity
    ) {
        Cart cart = cartService.addItemToCart(productId, quantity);
        CartDto cartDto = cartService.convertCartToDto(cart);

        return ResponseEntity.ok(
                new ApiResponse("Item added to cart successfully!", cartDto)
        );
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> updateItemQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity
    ) {
        Cart cart = cartService.updateItemQuantity(productId, quantity);
        CartDto cartDto = cartService.convertCartToDto(cart);

        return ResponseEntity.ok(
                new ApiResponse("Item quantity updated successfully!", cartDto)
        );
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> removeItem(
            @PathVariable Long productId
    ) {
        Cart cart = cartService.removeItemFromCart(productId);
        CartDto cartDto = cartService.convertCartToDto(cart);

        return ResponseEntity.ok(
                new ApiResponse("Item removed from cart successfully!", cartDto)
        );
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> clearCart() {
        cartService.clearCart();

        return ResponseEntity.ok(
                new ApiResponse("Cart cleared successfully!", null)
        );
    }
}
