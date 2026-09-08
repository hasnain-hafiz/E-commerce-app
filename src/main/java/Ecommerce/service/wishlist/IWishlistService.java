package Ecommerce.service.wishlist;

import Ecommerce.utils.dto.WishlistItemDto;

import java.util.List;

public interface IWishlistService {
    List<WishlistItemDto> getWishlist();
    WishlistItemDto addToWishlist(Long productId);
    void removeFromWishlist(Long productId);
}
