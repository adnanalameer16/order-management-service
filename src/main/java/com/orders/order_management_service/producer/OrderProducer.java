package com.orders.order_management_service.producer;

import com.orders.order_management_service.event.OrderPlacedEvent;
import com.orders.order_management_service.event.ReadyForShippingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order.created";
    public void sendOrderEvent(OrderPlacedEvent event) {

        Message<OrderPlacedEvent> message = MessageBuilder
                                            .withPayload(event)
                                            .setHeader(KafkaHeaders.TOPIC, TOPIC)
                                            .setHeader("event-type", "OrderPlaced")
                                            .build();

        kafkaTemplate.send(message);
        System.out.println("Message Sent to Kafka: " + event.getOrderId());

    }

    public void sendShippingEvent(ReadyForShippingEvent event) {

        Message<ReadyForShippingEvent> message = MessageBuilder
                                            .withPayload(event)
                                            .setHeader(KafkaHeaders.TOPIC, "order.ready_for_shipping")
                                            .setHeader("event-type", "ShippingCreated")
                                            .build();

        kafkaTemplate.send(message);
        System.out.println("Shipping Event Sent to Kafka");

    }
}
