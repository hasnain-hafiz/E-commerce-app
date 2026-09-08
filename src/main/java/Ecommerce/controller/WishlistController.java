package Ecommerce.controller;

import Ecommerce.service.wishlist.IWishlistService;
import Ecommerce.utils.dto.WishlistItemDto;
import Ecommerce.utils.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/wishlist")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://ecommerce-frontend-sigma-lilac.vercel.app")
public class WishlistController {

    private final IWishlistService wishlistService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getWishlist() {
        List<WishlistItemDto> wishlist = wishlistService.getWishlist();
        return ResponseEntity.ok(new ApiResponse("Wishlist fetched successfully!", wishlist));
    }

    @PostMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> addToWishlist(@PathVariable Long productId) {
        WishlistItemDto item = wishlistService.addToWishlist(productId);
        return ResponseEntity.ok(new ApiResponse("Added to wishlist!", item));
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> removeFromWishlist(@PathVariable Long productId) {
        wishlistService.removeFromWishlist(productId);
        return ResponseEntity.ok(new ApiResponse("Removed from wishlist!", null));
    }
}
