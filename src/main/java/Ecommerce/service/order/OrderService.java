package Ecommerce.service.order;

import Ecommerce.model.*;
import Ecommerce.model.user.User;
import Ecommerce.repository.OrderRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.service.cart.CartService;
import Ecommerce.utils.dto.OrderDto;
import Ecommerce.utils.dto.OrderItemDto;
import Ecommerce.utils.enums.OrderStatus;
import Ecommerce.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final CartService cartService;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    @Override
    public Order placeOrder(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        System.out.println("OrderService: User found: " + user.getEmail());
        Long cartId = user.getCart().getId();
        System.out.println("OrderService: Cart found: " + cartId);
        Cart cart = user.getCart();
        System.out.println("OrderService: Cart items: " + cart.getItems().size());

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = createOrder(cart);
        System.out.println("OrderService: Order created: " + order.getId() + order.getOrderStatus());
        Set<OrderItem> orderItems = createOrderItems(cart.getItems(), order);
        System.out.println("OrderService: Order items created: " + orderItems.size());
        order.setOrderItems(orderItems);

        orderRepository.save(order);
        System.out.println("OrderService: Order saved to database" + order.getId() + order.getOrderStatus());
        
        cartService.clearCart();
        System.out.println("OrderService: Cart cleared");
        return order;
    }

    private Order createOrder(Cart cart){
        Order order = new Order();
        order.setTotalAmount(cart.getTotalAmount());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setUser(cart.getUser());
        return order;
    }

    private Set<OrderItem> createOrderItems(List<CartItem> cartItems, Order order){
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
    public Order getOrderById(Long id, Long userId){
        return orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(()-> new IllegalArgumentException("Order not found"));
    }

    @Override
    @Transactional
    public Order cancelOrder(Long orderId, Long userId){
        Order order = getOrderById(orderId, userId);
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

    @Transactional
    @Override
    public List<Order> getAllOrders(Long userId){
        return orderRepository.findAllByUserId(userId);
    }

    @Override
    public List<OrderDto> convertAllOrdersToDto(List<Order> orders){
        return orders.stream().map(this::convertOrderToDto).toList();
    }

    @Override
    public OrderDto convertOrderToDto(Order order) {
        OrderDto orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setUserId(order.getUser().getId());
        orderDto.setStatus(order.getOrderStatus().name());
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
