package com.orders.order_management_service.controller;

import com.orders.order_management_service.dto.PaymentRequest;
import com.orders.order_management_service.event.PaymentCompletedEvent;
import com.orders.order_management_service.exception.ResourceNotFoundException;
import com.orders.order_management_service.producer.PaymentProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentProducer paymentProducer;

    @Autowired
    private OrderRepository orderRepository;


    @PostMapping("/payments/complete")
    public String processPayment(@RequestBody PaymentRequest request) {
        logger.info("Received payment request for Order ID: {}", request.getOrderId());

        if (!orderRepository.existsById(request.getOrderId())) {
            throw new ResourceNotFoundException("Order not found with ID: " + request.getOrderId());
        }

        PaymentCompletedEvent paymentCompletedEvent = new PaymentCompletedEvent(
            request.getOrderId(),
            request.getPaymentId()
        );

        paymentProducer.sendPaymentEvent(paymentCompletedEvent);
        logger.info("Payment event published for Order ID: {}", request.getOrderId());
        return "Payment Processed";
    }
}
