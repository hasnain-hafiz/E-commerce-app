package com.Ecom.E_commerce.app.service.cart;

import com.Ecom.E_commerce.app.dto.CartDto;
import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.model.CartItem;

import java.math.BigDecimal;
import java.util.List;

public interface ICartService {
    Cart getCart(Long cartId);
    Cart initializeCart();
    void clearCart(Long cartId);
    Cart addItemToCart(Long cartId, Long productId);
    Cart removeItemFromCart(Long cartId, Long productId);
    Cart updateItemQuantity(Long cartId, Long productId, int quantity);
    CartItem getCartItem(Long cartId, Long productId);
    CartDto convertToDto(Cart cart);
}
