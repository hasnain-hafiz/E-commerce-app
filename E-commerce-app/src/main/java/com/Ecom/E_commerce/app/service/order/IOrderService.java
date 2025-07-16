package com.Ecom.E_commerce.app.service.order;

import com.Ecom.E_commerce.app.model.Order;
import org.springframework.transaction.annotation.Transactional;

public interface IOrderService {

    Order placeOrder( Long userId);

    Order getOrderById(Long orderId, Long userId);

    Order cancelOrder(Long orderId , Long userId);
}
