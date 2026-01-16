package com.orders.order_management_service.producer;

import com.orders.order_management_service.event.OrderPlacedEvent;
import com.orders.order_management_service.event.ReadyForShippingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderProducer.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order.created";

    public void sendOrderEvent(OrderPlacedEvent event) {
        logger.info("Publishing OrderPlacedEvent for Order ID: {}", event.getOrderId());
        Message<OrderPlacedEvent> message = MessageBuilder
                                            .withPayload(event)
                                            .setHeader(KafkaHeaders.TOPIC, TOPIC)
                                            .setHeader("event-type", "OrderPlaced")
                                            .build();

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(message);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                logger.error("CRITICAL: Failed to publish OrderPlacedEvent for Order ID: {}", event.getOrderId(), ex);
            } else {
                logger.info("OrderPlacedEvent published successfully to topic: {}", TOPIC);
            }
        });
    }

    public void sendShippingEvent(ReadyForShippingEvent event) {
        logger.info("Publishing ReadyForShippingEvent for Order ID: {}", event.getOrderId());
        Message<ReadyForShippingEvent> message = MessageBuilder
                                            .withPayload(event)
                                            .setHeader(KafkaHeaders.TOPIC, "order.ready_for_shipping")
                                            .setHeader("event-type", "ShippingCreated")
                                            .build();

        kafkaTemplate.send(message);
        logger.debug("ReadyForShippingEvent published successfully to topic: order.ready_for_shipping");
    }
}
