package com.Ecom.E_commerce.app.service.order;

import com.Ecom.E_commerce.app.dto.OrderDto;
import com.Ecom.E_commerce.app.dto.OrderItemDto;
import com.Ecom.E_commerce.app.enums.OrderStatus;
import com.Ecom.E_commerce.app.model.*;
import com.Ecom.E_commerce.app.repository.OrderRepository;
import com.Ecom.E_commerce.app.repository.ProductRepository;
import com.Ecom.E_commerce.app.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Order placeOrder(Long cartId){
        Cart cart = cartService.getCart(cartId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        Order order = createOrder(cart);
        Set<OrderItem> orderItems = createOrderItems(cart.getItems(), order);
        order.setOrderItems(orderItems);

        orderRepository.save(order);
        cartService.clearCart(cartId);
        return order;
    }

    private Order createOrder(Cart cart){
        Order order = new Order();
        order.setTotalAmount(cart.getTotalAmount());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    private Set<OrderItem> createOrderItems(Set<CartItem> cartItems, Order order){
       return cartItems.stream().map(cartItem ->
                {
                    Product product = cartItem.getProduct();
                    int requestedQty = cartItem.getQuantity();

                    if (product.getInventory() < requestedQty) {
                        throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
                    }

                    product.setInventory(product.getInventory() - requestedQty);
                    productRepository.save(product);

                    OrderItem orderItem = new OrderItem();
                    orderItem.setPrice(cartItem.getUnitPrice());
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setOrder(order);
                    return orderItem;
                }
        ).collect(Collectors.toSet());
    }

    @Override
    public Order getOrderById(Long id){
        return orderRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Order not found"));
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId){
        Order order = getOrderById(orderId);
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }
        if (order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered order cannot be cancelled");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelDate(LocalDateTime.now());
       order.getOrderItems().forEach(orderItem -> {
           Product product = orderItem.getProduct();
           product.setInventory(product.getInventory() + orderItem.getQuantity());
           productRepository.save(product);
       });
        return orderRepository.save(order);
    }

   public OrderDto convertToDto(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        Set<OrderItemDto> orderItemDtos = new HashSet<>();
        order.getOrderItems().forEach(orderItem -> {
                   OrderItemDto dto =  modelMapper.map(orderItem, OrderItemDto.class);
                   dto.setProductName(orderItem.getProduct().getName());
                   orderItemDtos.add(dto);
                });
        orderDto.setItems(orderItemDtos);
        return orderDto;
    }
}
