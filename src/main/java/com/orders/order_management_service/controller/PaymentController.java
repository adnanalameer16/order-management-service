package com.orders.order_management_service.controller;

import com.orders.order_management_service.event.PaymentCompletedEvent;
import com.orders.order_management_service.producer.PaymentProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    @Autowired
    private PaymentProducer paymentProducer;


    @PostMapping("/payments/complete")
    public String processPayment(@RequestBody PaymentCompletedEvent paymentCompletedEvent) {

        paymentProducer.sendPaymentEvent(paymentCompletedEvent);
        return "Payment Processed";
    }
}
