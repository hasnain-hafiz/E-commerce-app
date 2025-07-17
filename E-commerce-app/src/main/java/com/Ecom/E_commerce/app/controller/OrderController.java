package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.repository.UserRepository;
import com.Ecom.E_commerce.app.utils.dto.OrderDto;
import com.Ecom.E_commerce.app.utils.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Order;
import com.Ecom.E_commerce.app.utils.response.ApiResponse;
import com.Ecom.E_commerce.app.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    public ResponseEntity<ApiResponse> placeOrder(){
        try {
            Order order = orderService.placeOrder(getCurrentUserId());
            OrderDto orderDto = orderService.convertOrderToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order placed successfully!", orderDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getAllOrders(){
        try {
            List<Order> orders = orderService.getAllOrders(getCurrentUserId());
            List<OrderDto> orderDtos = orderService.convertAllOrdersToDto(orders);
            return ResponseEntity.ok(new ApiResponse("Orders fetched successfully!", orderDtos));
        }
        catch (Exception e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/all/by-userId/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllOrdersByUserId(@PathVariable Long userId){
        try {
            List<Order> orders = orderService.getAllOrders(userId);
            List<OrderDto> orderDtos = orderService.convertAllOrdersToDto(orders);
            return ResponseEntity.ok(new ApiResponse("Orders fetched successfully!", orderDtos));
        }
        catch (Exception e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }


    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId){
        try {
            Order order = orderService.getOrderById(orderId, getCurrentUserId());
            OrderDto orderDto = orderService.convertOrderToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order fetched successfully!", orderDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PutMapping("/cancel/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long orderId){
        try {
            Order order = orderService.cancelOrder(orderId, getCurrentUserId());
            OrderDto orderDto = orderService.convertOrderToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order cancelled successfully!", orderDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }
}
