package Ecommerce.service.cart;

import Ecommerce.model.Cart;
import Ecommerce.utils.dto.CartDto;

public interface ICartService {
    Cart getCart();

    // =========================
    // ➕ ADD ITEM (WITH QUANTITY)
    // =========================
    Cart addItemToCart(Long productId, int quantity);

    // =========================
    // 🔄 UPDATE QUANTITY
    // =========================
    Cart updateItemQuantity(Long productId, int quantity);

    // =========================
    // ❌ REMOVE ITEM
    // =========================
    Cart removeItemFromCart(Long productId);

    // =========================
    // 🧹 CLEAR CART
    // =========================
    void clearCart();


    // =========================
    // 📦 DTO CONVERSION
    // =========================
    CartDto convertCartToDto(Cart cart);
}
