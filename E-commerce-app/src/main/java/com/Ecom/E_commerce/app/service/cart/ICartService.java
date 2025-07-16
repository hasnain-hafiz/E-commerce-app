package com.Ecom.E_commerce.app.service.cart;

import com.Ecom.E_commerce.app.model.user.CustomUserDetails;
import com.Ecom.E_commerce.app.utils.dto.CartDto;
import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.model.CartItem;

public interface ICartService {
    Cart getCart(Long cartId, Long userId);
    Cart initializeCart(Long userId);
    void clearCart(Long cartId, Long userId);
    Cart addItemToCart(Long cartId, Long productId, Long userId);
    Cart removeItemFromCart(Long cartId, Long productId , Long userId);
    Cart updateItemQuantity(Long cartId, Long productId, int quantity, Long userId);
    CartItem getCartItem(Long cartId, Long productId , Long userId);
    CartDto convertToDto(Cart cart);
}
