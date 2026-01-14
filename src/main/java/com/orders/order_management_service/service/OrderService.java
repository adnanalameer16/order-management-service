package com.orders.order_management_service.service;

import com.orders.order_management_service.event.OrderPlacedEvent;
import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.model.OrderItem;
import com.orders.order_management_service.model.OrderStatus;
import com.orders.order_management_service.producer.OrderProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer orderProducer;


    public Order createOrder(List<OrderItem> items) {
        String orderId = UUID.randomUUID().toString();
        Order order = new Order(items,orderId, OrderStatus.PENDING_PAYMENT, "test-customer");
        Order savedOrder = orderRepository.save(order);

        OrderPlacedEvent event = new OrderPlacedEvent(
                savedOrder.getOrderId(),
                savedOrder.getSubtotal(),
                savedOrder.getItems().size()
        );

        orderProducer.sendOrderEvent(event);

        return savedOrder;

    }

    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }
}