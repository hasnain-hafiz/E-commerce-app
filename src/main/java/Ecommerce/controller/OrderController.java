package Ecommerce.controller;

import Ecommerce.model.Order;
import Ecommerce.repository.UserRepository;
import Ecommerce.service.order.OrderService;
import Ecommerce.utils.dto.OrderDto;
import Ecommerce.utils.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email))
                .getId();
    }

    @PostMapping("/placeOrder")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder() {
        Order order = orderService.placeOrder(getCurrentUserId());
        return ResponseEntity.ok(new ApiResponse<>("Order placed successfully!", orderService.convertOrderToDto(order)));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders(getCurrentUserId());
        return ResponseEntity.ok(new ApiResponse<>("Orders fetched successfully!", orderService.convertAllOrdersToDto(orders)));
    }

    // Admin oversight view - scoped to a specific user's orders, not a global dump.
    @GetMapping("/all/by-userId/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getAllOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getAllOrders(userId);
        return ResponseEntity.ok(new ApiResponse<>("Orders fetched successfully!", orderService.convertAllOrdersToDto(orders)));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId, getCurrentUserId());
        return ResponseEntity.ok(new ApiResponse<>("Order fetched successfully!", orderService.convertOrderToDto(order)));
    }

    @PutMapping("/cancel/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(@PathVariable Long orderId) {
        Order order = orderService.cancelOrder(orderId, getCurrentUserId());
        return ResponseEntity.ok(new ApiResponse<>("Order cancelled successfully!", orderService.convertOrderToDto(order)));
    }
}
