package com.orders.order_management_service.service;

import com.orders.order_management_service.dto.OrderRequest;
import com.orders.order_management_service.dto.OrderResponse;
import com.orders.order_management_service.event.OrderPlacedEvent;
import com.orders.order_management_service.exception.ResourceNotFoundException;
import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.model.OrderStatus;
import com.orders.order_management_service.producer.OrderProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer orderProducer;

    @Value("${order.tax-rate}")
    private double defaultTaxRate;

    public OrderResponse createOrder(OrderRequest request) {
        String orderId = UUID.randomUUID().toString();
        Double effectiveTaxRate;
        if (request.getTaxRate() != null) {
            effectiveTaxRate = request.getTaxRate();
        } else {
            effectiveTaxRate = defaultTaxRate;
        }
        Order order = new Order(request.getOrderItems(), orderId, OrderStatus.PENDING_PAYMENT, request.getCustomerId(), effectiveTaxRate);
        Order savedOrder = orderRepository.save(order);
        OrderPlacedEvent event = new OrderPlacedEvent(
                "order.created",
                savedOrder.getOrderId(),
                Instant.now().toString(),
                savedOrder.getCustomerId(),
                savedOrder.getSubtotal(),
                savedOrder.getTaxAmount(),
                savedOrder.getTotalAmount()
        );
        orderProducer.sendOrderEvent(event);

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getOrdersByCustomer(String CustomerId) {
        List<Order> orders = orderRepository.findByCustomerId(CustomerId);
        return orders.stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getOrderId(),
                order.getOrderStatus().toString(),
                order.getCustomerId(),
                order.getSubtotal(),
                order.getTaxRate(),
                order.getTaxAmount(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems()
        );
    }
}