package com.orders.order_management_service.consumer;


import com.orders.order_management_service.event.PaymentCompletedEvent;
import com.orders.order_management_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    @KafkaListener(topics = "payment-events", groupId = "order-group")
    public void handlePaymentEvent(PaymentCompletedEvent event) {

        System.out.println("Received Payment Completed Event for Order ID: " + event.getOrderId());
    }
}
