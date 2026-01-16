package com.orders.order_management_service.service;

import com.orders.order_management_service.dto.OrderRequest;
import com.orders.order_management_service.dto.OrderResponse;
import com.orders.order_management_service.event.OrderPlacedEvent;
import com.orders.order_management_service.exception.ResourceNotFoundException;
import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.model.OrderStatus;
import com.orders.order_management_service.producer.OrderProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer orderProducer;

    @Value("${order.tax-rate}")
    private double defaultTaxRate;

    public OrderResponse createOrder(OrderRequest request) {
        logger.info("Creating order for customer: {}", request.getCustomerId());
        String orderId = UUID.randomUUID().toString();
        Double effectiveTaxRate;
        if (request.getTaxRate() != null) {
            effectiveTaxRate = request.getTaxRate();
        } else {
            effectiveTaxRate = defaultTaxRate;
        }
        Order order = new Order(request.getOrderItems(), orderId, OrderStatus.PENDING_PAYMENT, request.getCustomerId(), effectiveTaxRate);
        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with ID: {}", savedOrder.getOrderId());
        OrderPlacedEvent event = new OrderPlacedEvent(
                "order.created",
                savedOrder.getOrderId(),
                Instant.now().toString(),
                savedOrder.getCustomerId(),
                savedOrder.getSubtotal(),
                savedOrder.getTaxAmount(),
                savedOrder.getTotalAmount(),
                MDC.get("correlationId")
        );
        orderProducer.sendOrderEvent(event);

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrder(String orderId) {
        logger.info("Fetching order details for ID: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        return mapToOrderResponse(order);
    }

    public Page<OrderResponse> getOrdersByCustomer(String CustomerId, Pageable pageable) {
        logger.info("Fetching orders for customer: {}", CustomerId);
        Page<Order> orders = orderRepository.findByCustomerId(CustomerId, pageable);
        return orders.map(this::mapToOrderResponse);
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