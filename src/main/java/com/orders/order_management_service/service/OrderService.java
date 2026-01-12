package com.orders.order_management_service.service;

import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.model.OrderItem;
import com.orders.order_management_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;


    public Order createOrder(List<OrderItem> items) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(items,orderId);
        return orderRepository.save(order);
    }
}
