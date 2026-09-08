package Ecommerce.service.wishlist;

import Ecommerce.model.Product;
import Ecommerce.model.WishlistItem;
import Ecommerce.model.user.User;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.repository.WishlistItemRepository;
import Ecommerce.service.product.ProductService;
import Ecommerce.utils.dto.WishlistItemDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService implements IWishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<WishlistItemDto> getWishlist() {
        Long userId = getCurrentUser().getId();
        return wishlistItemRepository.findByUserIdOrderByAddedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public WishlistItemDto addToWishlist(Long productId) {
        User user = getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        // Idempotent by design: re-adding an already-wishlisted product
        // returns the existing entry rather than erroring or duplicating —
        // "add to wishlist" behaves like a toggle from the UI's perspective.
        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseGet(() -> wishlistItemRepository.save(new WishlistItem(user, product)));

        return convertToDto(item);
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long productId) {
        Long userId = getCurrentUser().getId();
        wishlistItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    private WishlistItemDto convertToDto(WishlistItem item) {
        WishlistItemDto dto = new WishlistItemDto();
        dto.setId(item.getId());
        dto.setProduct(productService.convertToDto(item.getProduct()));
        dto.setAddedAt(item.getAddedAt());
        return dto;
    }
}
