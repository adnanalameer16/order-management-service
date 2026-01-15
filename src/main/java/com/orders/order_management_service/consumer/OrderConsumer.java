package com.orders.order_management_service.consumer;


import com.orders.order_management_service.event.PaymentCompletedEvent;
import com.orders.order_management_service.event.ReadyForShippingEvent;
import com.orders.order_management_service.model.OrderStatus;
import com.orders.order_management_service.producer.OrderProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OrderConsumer {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer orderProducer;

    @KafkaListener(topics = "payment.completed", groupId = "order-group")
    public void handlePaymentEvent(PaymentCompletedEvent event) {
            orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
                order.setOrderStatus(OrderStatus.PAID);
                orderRepository.save(order);
                System.out.println("Received Payment Completed Event for Order ID: " + event.getOrderId());
                order.setUpdatedAt(Instant.now().toString());

                ReadyForShippingEvent readyForShippingEvent = new ReadyForShippingEvent(event.getOrderId());
                orderProducer.sendShippingEvent(readyForShippingEvent);
                System.out.println("Sent Ready For Shipping Event for Order ID: " + readyForShippingEvent.getOrderId());
            } , () -> {
                System.out.println("Order not found for Order ID: " + event.getOrderId());
            });
    }
}
