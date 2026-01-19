package com.orders.order_management_service;

import com.orders.order_management_service.consumer.OrderConsumer;
import com.orders.order_management_service.event.PaymentCompletedEvent;
import com.orders.order_management_service.event.ReadyForShippingEvent;
import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.model.OrderStatus;
import com.orders.order_management_service.producer.OrderProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderConsumerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProducer orderProducer;

    @InjectMocks
    private OrderConsumer orderConsumer;

    @Test
    void updateStatusTest() {
        String orderId = "order123";
        String correlationId = "corr123";
        PaymentCompletedEvent event = new PaymentCompletedEvent(orderId, "payment123", correlationId);

        Order existingOrder = new Order(null, orderId, OrderStatus.PENDING_PAYMENT, "cust-1", 0.1);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        orderConsumer.handlePaymentEvent(event);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assert(savedOrder.getOrderStatus() == OrderStatus.PAID);

        ArgumentCaptor<ReadyForShippingEvent> eventCaptor = ArgumentCaptor.forClass(ReadyForShippingEvent.class);
        verify(orderProducer).sendShippingEvent(eventCaptor.capture());
        assertEquals(orderId, eventCaptor.getValue().getOrderId());
        assertEquals(correlationId, eventCaptor.getValue().getCorrelationId());
    }
}