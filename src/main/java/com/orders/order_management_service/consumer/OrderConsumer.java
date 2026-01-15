package com.orders.order_management_service.consumer;


import com.orders.order_management_service.event.PaymentCompletedEvent;
import com.orders.order_management_service.event.ReadyForShippingEvent;
import com.orders.order_management_service.model.OrderStatus;
import com.orders.order_management_service.producer.OrderProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer orderProducer;

    @KafkaListener(topics = "payment.completed", groupId = "order-group")
    public void handlePaymentEvent(PaymentCompletedEvent event) {
        logger.info("Received PaymentCompletedEvent for Order ID: {}", event.getOrderId());
        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
                order.setOrderStatus(OrderStatus.PAID);
                order.setUpdatedAt(Instant.now().toString());
                orderRepository.save(order);
                logger.info("Order status updated to PAID for Order ID: {}", event.getOrderId());

                ReadyForShippingEvent readyForShippingEvent = new ReadyForShippingEvent(event.getOrderId());
                orderProducer.sendShippingEvent(readyForShippingEvent);
            } , () -> {
                logger.warn("Order not found for Order ID: {}", event.getOrderId());
            });
    }
}
