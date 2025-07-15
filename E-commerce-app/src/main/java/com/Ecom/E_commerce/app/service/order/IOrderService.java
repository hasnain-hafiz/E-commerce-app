package com.Ecom.E_commerce.app.service.order;

import com.Ecom.E_commerce.app.model.Order;

public interface IOrderService {
    Order placeOrder(Long cartId);
    Order getOrderById(Long orderId);

    Order cancelOrder(Long orderId);
}
