package com.orders.order_management_service.producer;

import com.orders.order_management_service.event.PaymentCompletedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "payment.completed";

    public void sendPaymentEvent(PaymentCompletedEvent event) {
        Message<PaymentCompletedEvent> message = MessageBuilder
                                                    .withPayload(event)
                                                    .setHeader(KafkaHeaders.TOPIC, TOPIC)
                                                    .setHeader("event-type", "PaymentCompleted")
                                                    .build();
        kafkaTemplate.send(message);
        System.out.println("Payment Event Sent to Kafka for Order ID: " + event.getOrderId());
    }
}
