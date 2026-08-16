package Ecommerce.controller;

import Ecommerce.model.Cart;
import Ecommerce.service.cart.CartService;
import Ecommerce.utils.dto.CartDto;
import Ecommerce.utils.response.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> getCart() {
        Cart cart = cartService.getCart();
        return ResponseEntity.ok(new ApiResponse<>("Cart fetched successfully!", cartService.convertCartToDto(cart)));
    }

    @PostMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") @Min(1) @Max(100) int quantity
    ) {
        Cart cart = cartService.addItemToCart(productId, quantity);
        return ResponseEntity.ok(new ApiResponse<>("Item added to cart successfully!", cartService.convertCartToDto(cart)));
    }

    @PutMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> updateItemQuantity(
            @PathVariable Long productId,
            @RequestParam @Min(1) @Max(100) int quantity
    ) {
        Cart cart = cartService.updateItemQuantity(productId, quantity);
        return ResponseEntity.ok(new ApiResponse<>("Item quantity updated successfully!", cartService.convertCartToDto(cart)));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(@PathVariable Long productId) {
        Cart cart = cartService.removeItemFromCart(productId);
        return ResponseEntity.ok(new ApiResponse<>("Item removed from cart successfully!", cartService.convertCartToDto(cart)));
    }

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(new ApiResponse<>("Cart cleared successfully!", null));
    }
}
