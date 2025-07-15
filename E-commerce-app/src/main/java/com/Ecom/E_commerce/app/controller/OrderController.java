package com.Ecom.E_commerce.app.controller;

import com.Ecom.E_commerce.app.dto.OrderDto;
import com.Ecom.E_commerce.app.exceptions.ResourceNotFoundException;
import com.Ecom.E_commerce.app.model.Order;
import com.Ecom.E_commerce.app.response.ApiResponse;
import com.Ecom.E_commerce.app.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("${api.prefix}/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/placeOrder/{cartId}")
    public ResponseEntity<ApiResponse> placeOrder(@PathVariable Long cartId){
        try {
            Order order = orderService.placeOrder(cartId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order placed successfully!", orderDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long orderId){
        try {
            Order order = orderService.getOrderById(orderId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order fetched successfully!", orderDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long orderId){
        try {
            Order order = orderService.cancelOrder(orderId);
            OrderDto orderDto = orderService.convertToDto(order);
            return ResponseEntity.ok(new ApiResponse("Order cancelled successfully!", orderDto));
        }
        catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("error", e.getMessage()));
        }
    }
}
