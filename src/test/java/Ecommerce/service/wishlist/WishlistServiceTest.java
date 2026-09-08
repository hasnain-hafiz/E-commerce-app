package Ecommerce.service.wishlist;

import Ecommerce.model.Product;
import Ecommerce.model.WishlistItem;
import Ecommerce.model.user.User;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.repository.WishlistItemRepository;
import Ecommerce.service.product.ProductService;
import Ecommerce.utils.dto.ProductDto;
import Ecommerce.utils.dto.WishlistItemDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock private WishlistItemRepository wishlistItemRepository;
    @Mock private ProductRepository productRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductService productService;

    @InjectMocks
    private WishlistService wishlistService;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("buyer@shop.com");

        product = new Product();
        product.setId(50L);
        product.setName("Desk Lamp");

        when(userRepository.findByEmail("buyer@shop.com")).thenReturn(Optional.of(user));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("buyer@shop.com", null));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addToWishlist_createsNewEntry_whenNotAlreadyWishlisted() {
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByUserIdAndProductId(1L, 50L)).thenReturn(Optional.empty());
        when(wishlistItemRepository.save(any(WishlistItem.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(productService.convertToDto(product)).thenReturn(new ProductDto());

        WishlistItemDto result = wishlistService.addToWishlist(50L);

        assertNotNull(result);
        verify(wishlistItemRepository).save(any(WishlistItem.class));
    }

    @Test
    void addToWishlist_isIdempotent_whenAlreadyWishlisted() {
        WishlistItem existing = new WishlistItem(user, product);
        when(productRepository.findById(50L)).thenReturn(Optional.of(product));
        when(wishlistItemRepository.findByUserIdAndProductId(1L, 50L)).thenReturn(Optional.of(existing));
        when(productService.convertToDto(product)).thenReturn(new ProductDto());

        wishlistService.addToWishlist(50L);

        // Must not create a duplicate row for an already-wishlisted product.
        verify(wishlistItemRepository, never()).save(any());
    }

    @Test
    void addToWishlist_throwsNotFound_whenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.addToWishlist(999L));
    }

    @Test
    void removeFromWishlist_delegatesToRepositoryForCurrentUserOnly() {
        wishlistService.removeFromWishlist(50L);

        verify(wishlistItemRepository).deleteByUserIdAndProductId(1L, 50L);
    }

    @Test
    void getWishlist_mapsEachItemThroughProductService() {
        WishlistItem item = new WishlistItem(user, product);
        when(wishlistItemRepository.findByUserIdOrderByAddedAtDesc(1L)).thenReturn(List.of(item));
        when(productService.convertToDto(product)).thenReturn(new ProductDto());

        List<WishlistItemDto> result = wishlistService.getWishlist();

        assertEquals(1, result.size());
        verify(productService).convertToDto(product);
    }
}
