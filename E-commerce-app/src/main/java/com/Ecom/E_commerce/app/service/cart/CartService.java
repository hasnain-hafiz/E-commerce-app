package com.Ecom.E_commerce.app.service.cart;

import com.Ecom.E_commerce.app.dto.CartDto;
import com.Ecom.E_commerce.app.dto.CartItemDto;
import com.Ecom.E_commerce.app.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Cart;
import com.Ecom.E_commerce.app.model.CartItem;
import com.Ecom.E_commerce.app.model.Product;
import com.Ecom.E_commerce.app.repository.CartRepository;
import com.Ecom.E_commerce.app.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService{

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public Cart getCart(Long id) {
       return cartRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("cart not found"));
    }

    @Override
    public Cart initializeCart() {
        Cart cart = new Cart();
        return cartRepository.save(cart);
    }

    @Override
    public void clearCart(Long Id) {
        Cart cart = getCart(Id);
        cart.getItems().clear();
        cart.recalculateTotal();
        cartRepository.save(cart);
    }

    @Override
    public Cart addItemToCart(Long cartId, Long productId) {
        Cart cart = getCart(cartId);
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
    public Cart removeItemFromCart(Long cartId, Long productId) {
        Cart cart = getCart(cartId);
        CartItem cartItem = getCartItem(cartId, productId);
        cart.removeItem(cartItem);

        return cartRepository.save(cart);
    }

    @Override
    public Cart updateItemQuantity(Long cartId, Long productId, int quantity) {
        if (quantity <= 0) {
            removeItemFromCart(cartId, productId);
            return getCart(cartId);
        }

        Cart cart = getCart(cartId);
        CartItem item = getCartItem(cartId, productId);

        item.setQuantity(quantity);
        item.setUnitPrice(item.getProduct().getPrice());
        item.setTotalPrice();
        cart.recalculateTotal();

        return cartRepository.save(cart);
    }

    @Override
    public CartItem getCartItem(Long cartId, Long productId) {
        Cart cart = getCart(cartId);
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
