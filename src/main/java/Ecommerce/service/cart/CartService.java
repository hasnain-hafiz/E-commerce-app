package Ecommerce.service.cart;

import Ecommerce.model.Cart;
import Ecommerce.model.CartItem;
import Ecommerce.model.Product;
import Ecommerce.model.user.User;
import Ecommerce.repository.CartRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.utils.dto.CartDto;
import Ecommerce.utils.dto.CartItemDto;
import Ecommerce.utils.exceptions.ResourceNotFoundException;
import Ecommerce.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService implements ICartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    // =========================
    // 🔐 USER CONTEXT
    // =========================
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    // =========================
    // 🛒 GET OR CREATE CART
    // =========================
    public Cart getOrCreateCart() {

        User user = getCurrentUser();
        return Optional.ofNullable(user.getCart())
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUser(user);

                    return cartRepository.save(cart);
                });
    }

    // =========================
    // 📦 GET CART
    // =========================
    @Override
    public Cart getCart() {
        return getOrCreateCart();
    }

    // =========================
    // ➕ ADD ITEM (WITH QUANTITY)
    // =========================
    @Override
    public Cart addItemToCart(Long productId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getOrCreateCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getInventory() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        CartItem item = findCartItem(cart, productId);

        if (item == null) {
            item = createCartItem(product, quantity);
            cart.addItem(item);
        } else {
            int newQty = item.getQuantity() + quantity;

            if (product.getInventory() < newQty) {
                throw new IllegalArgumentException("Not enough stock available");
            }

            item.setQuantity(newQty);
            item.setTotalPrice();
        }

        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    // =========================
    // 🔄 UPDATE QUANTITY
    // =========================
    @Override
    public Cart updateItemQuantity(Long productId, int quantity) {

        Cart cart = getOrCreateCart();

        if (quantity <= 0) {
            return removeItemFromCart(productId);
        }

        CartItem item = findCartItem(cart, productId);

        if (item == null) {
            throw new ResourceNotFoundException("Item not found in cart");
        }

        Product product = item.getProduct();

        if (product.getInventory() < quantity) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        item.setQuantity(quantity);
        item.setTotalPrice();

        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    // =========================
    // ❌ REMOVE ITEM
    // =========================
    @Override
    public Cart removeItemFromCart(Long productId) {
        Cart cart = getOrCreateCart();

        CartItem item = findCartItem(cart, productId);

        if (item == null) {
            throw new ResourceNotFoundException("Item not found in cart");
        }

        cart.removeItem(item);
        cart.recalculateTotal();

        return cartRepository.save(cart);
    }

    // =========================
    // 🧹 CLEAR CART
    // =========================
    @Override
    public void clearCart() {
        Cart cart = getOrCreateCart();

        cart.getItems().forEach(item -> item.setCart(null));
        cart.getItems().clear();
        cart.recalculateTotal();

        cartRepository.save(cart);
    }

    // =========================
    // 🔍 HELPERS
    // =========================
    private CartItem findCartItem(Cart cart, Long productId) {
        return cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);
    }

    private CartItem createCartItem(Product product, int quantity) {
        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());
        item.setTotalPrice();
        return item;
    }



    // =========================
    // 📦 DTO CONVERSION
    // =========================
    @Override
    public CartDto convertCartToDto(Cart cart) {

        CartDto cartDto = modelMapper.map(cart, CartDto.class);

        List<CartItemDto> items = cart.getItems()
                .stream()
                .map(item -> {

                    CartItemDto dto = modelMapper.map(item, CartItemDto.class);

                    dto.setProductId(item.getProduct().getId());


                    dto.setProductName(item.getProduct().getName());

                    return dto;
                })
                .collect(Collectors.toList());


        cartDto.setItems(items);

        return cartDto;
    }
}