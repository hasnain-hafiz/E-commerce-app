package com.Ecom.E_commerce.app.service.cart;

import com.Ecom.E_commerce.app.model.user.CustomUserDetails;
import com.Ecom.E_commerce.app.model.user.User;
import com.Ecom.E_commerce.app.repository.UserRepository;
import com.Ecom.E_commerce.app.utils.dto.CartDto;
import com.Ecom.E_commerce.app.utils.dto.CartItemDto;
import com.Ecom.E_commerce.app.utils.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.model.CartItem;
import com.Ecom.E_commerce.app.model.Product;
import com.Ecom.E_commerce.app.repository.CartRepository;
import com.Ecom.E_commerce.app.repository.ProductRepository;
import com.Ecom.E_commerce.app.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService{

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    @Override
    public Cart getCart(Long id, Long userId) {
       return cartRepository.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new ResourceNotFoundException("cart not found"));
    }

    @Override
    public Cart initializeCart(Long userId) {
        Cart cart = new Cart();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long Id, Long userId) {
        Cart cart = getCart(Id,userId);
        cart.getItems().clear();
        cart.recalculateTotal();
        cartRepository.save(cart);
    }

    @Override
    public Cart addItemToCart(Long cartId, Long productId, Long userId) {
        Cart cart = getCart(cartId, userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if(product.getInventory() <= 0){
            throw new IllegalArgumentException("Product is out of stock");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);


        if(cartItem == null){
            CartItem newCartItem = new CartItem();
            newCartItem.setProduct(product);
            newCartItem.setQuantity(1);
            newCartItem.setUnitPrice(product.getPrice());
            newCartItem.setTotalPrice();
            cart.addItem(newCartItem);
        }
        else{
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItem.setTotalPrice();

        }
        cart.recalculateTotal();
        return cartRepository.save(cart);
    }

    @Override
    public Cart removeItemFromCart(Long cartId, Long productId, Long userId) {
        Cart cart = getCart(cartId, userId);
        CartItem cartItem = getCartItem(cartId, productId, userId);
        cart.removeItem(cartItem);

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateItemQuantity(Long cartId, Long productId, int quantity, Long userId) {
        if (quantity <= 0) {
            removeItemFromCart(cartId, productId, userId);
            return getCart(cartId,userId);
        }

        Cart cart = getCart(cartId, userId);
        CartItem item = getCartItem(cartId, productId, userId);

        item.setQuantity(quantity);
        item.setUnitPrice(item.getProduct().getPrice());
        item.setTotalPrice();
        cart.recalculateTotal();

        return cartRepository.save(cart);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productId, Long userId) {
        Cart cart = getCart(cartId, userId);
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return cartItem;
    }

    @Override
    public CartDto convertToDto(Cart cart){
        CartDto cartDto = modelMapper.map(cart, CartDto.class);
        Set<CartItemDto> cartItemDtos = new HashSet<>();
        cart.getItems().forEach(item -> {
            CartItemDto dto = modelMapper.map(item, CartItemDto.class);
            dto.setProductName(item.getProduct().getName()); // 👈 manually set
            cartItemDtos.add(dto);
        });

        cartDto.setItems(cartItemDtos);
        return cartDto;
    }
}
