package com.Ecom.E_commerce.app.service.order;

import com.Ecom.E_commerce.app.model.Order;
import com.Ecom.E_commerce.app.utils.dto.OrderDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOrderService {

    Order placeOrder( Long userId);

    Order getOrderById(Long orderId, Long userId);

    Order cancelOrder(Long orderId , Long userId);

    List<Order> getAllOrders(Long userId);

    List<OrderDto> convertAllOrdersToDto(List<Order> orders);

    OrderDto convertOrderToDto(Order order);
}
