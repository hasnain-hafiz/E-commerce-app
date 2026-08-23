package Ecommerce.service.order;

import Ecommerce.model.Cart;
import Ecommerce.model.CartItem;
import Ecommerce.model.Order;
import Ecommerce.model.Product;
import Ecommerce.model.user.User;
import Ecommerce.repository.OrderRepository;
import Ecommerce.repository.ProductRepository;
import Ecommerce.repository.UserRepository;
import Ecommerce.service.cart.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private CartService cartService;
    @Mock private ProductRepository productRepository;
    @Mock private ModelMapper modelMapper;
    @Mock private UserRepository userRepository;
    @Mock private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);

        product = new Product();
        product.setId(10L);
        product.setName("Wireless Mouse");
        // Live/current price at checkout time.
        product.setPrice(new BigDecimal("80.00"));
        product.setInventory(5);

        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        // Stale price captured when the item was added to the cart —
        // deliberately different from product.getPrice() to prove the
        // order snapshots the live price, not this one.
        cartItem.setUnitPrice(new BigDecimal("50.00"));
        cartItem.setTotalPrice(new BigDecimal("100.00"));

        List<CartItem> items = new ArrayList<>();
        items.add(cartItem);

        cart = new Cart();
        cart.setUser(user);
        cart.setItems(items);

        user.setCart(cart);
    }

    @Test
    void placeOrder_snapshotsLiveProductPrice_notStaleCartPrice() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = orderService.placeOrder(1L);

        assertEquals(1, order.getOrderItems().size());
        BigDecimal snapshottedPrice = order.getOrderItems().iterator().next().getPrice();

        // Must use the live product price (80), not the stale cart price (50).
        assertEquals(0, new BigDecimal("80.00").compareTo(snapshottedPrice));
        // Total must be recalculated from the live price: 2 * 80 = 160, not
        // the cart's stale total of 100.
        assertEquals(0, new BigDecimal("160.00").compareTo(order.getTotalAmount()));

        verify(cartService).clearCart();
    }

    @Test
    void placeOrder_decrementsInventoryBySnapshottedQuantity() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(1L);

        assertEquals(3, product.getInventory()); // started at 5, ordered 2
        verify(productRepository).save(product);
    }

    @Test
    void placeOrder_throwsIllegalArgument_whenRequestedQuantityExceedsStock() {
        product.setInventory(1); // less than the requested quantity of 2
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(1L));
        verify(orderRepository, never()).save(any());
        verify(cartService, never()).clearCart();
    }

    @Test
    void placeOrder_throwsIllegalArgument_whenCartIsEmpty() {
        cart.setItems(new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder(1L));
    }
}
