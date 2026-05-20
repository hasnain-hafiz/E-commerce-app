package Ecommerce.service.order;

import Ecommerce.model.Order;
import Ecommerce.utils.dto.OrderDto;

import java.util.List;

public interface IOrderService {

    Order placeOrder( Long userId);

    Order getOrderById(Long orderId, Long userId);

    Order cancelOrder(Long orderId , Long userId);

    List<Order> getAllOrders(Long userId);

    List<OrderDto> convertAllOrdersToDto(List<Order> orders);

    OrderDto convertOrderToDto(Order order);
}
