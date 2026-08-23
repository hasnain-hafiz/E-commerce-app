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

import java.math.BigDecimal;
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
        Cart cart = user.getCart();

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = createOrder(cart);
        Set<OrderItem> orderItems = createOrderItems(cart.getItems(), order);
        order.setOrderItems(orderItems);

        // CHANGED: previously order.totalAmount was copied from
        // cart.getTotalAmount(), which was computed from each CartItem's
        // unitPrice — captured once, when the item was *added* to the
        // cart, and never refreshed. If a seller changed the product price
        // while it sat in the cart, the customer would check out at a
        // stale price. Now the total is recomputed from the prices actually
        // snapshotted onto the order items below (which read the live
        // Product price), so the backend remains authoritative for pricing
        // at the moment of purchase, not at the moment of add-to-cart.
        order.setTotalAmount(recalculateTotal(orderItems));

        orderRepository.save(order);

        cartService.clearCart();
        return order;
    }

    private BigDecimal recalculateTotal(Set<OrderItem> orderItems) {
        return orderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrder(Cart cart){
        Order order = new Order();
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
                    // CHANGED: no manual flush needed for the @Version bump —
                    // saving here means if another transaction already
                    // incremented this row's version (e.g. a concurrent
                    // checkout), Hibernate raises
                    // ObjectOptimisticLockingFailureException instead of
                    // silently overwriting the other purchase's decrement.
                    // See GlobalExceptionHandler for how that's surfaced.
                    productRepository.save(product);

                    OrderItem orderItem = new OrderItem();
                    // CHANGED: was cartItem.getUnitPrice() (stale, captured
                    // at add-to-cart time). Now snapshots the live product
                    // price at the moment of purchase, which is what
                    // "price snapshot at purchase time" is supposed to mean.
                    orderItem.setPrice(product.getPrice());
                    orderItem.setProduct(product);
                    orderItem.setQuantity(requestedQty);
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
