package com.orders.order_management_service;

import com.orders.order_management_service.dto.OrderRequest;
import com.orders.order_management_service.dto.OrderResponse;
import com.orders.order_management_service.event.OrderPlacedEvent;
import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.model.OrderItem;
import com.orders.order_management_service.model.OrderStatus;
import com.orders.order_management_service.producer.OrderProducer;
import com.orders.order_management_service.repository.OrderRepository;
import com.orders.order_management_service.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProducer orderProducer;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "defaultTaxRate",0.1);
    }

    @Test
    void testCreateOrder_Successful() {
        OrderItem item = new OrderItem("Laptop", 100.0, 2);
        OrderRequest request = new OrderRequest(List.of(item), null, "test-customer");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation ->{
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setOrderId("order123");
            return orderToSave;
        });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("order123", response.getOrderId());
        assertEquals("test-customer", response.getCustomerId());
        assertEquals(200.0, response.getSubtotal());
        assertEquals(20.0, response.getTaxAmount());
        assertEquals(220.0, response.getTotalAmount());
        assertEquals("PENDING_PAYMENT", response.getOrderStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProducer, times(1)).sendOrderEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void testCreateOrder_WithCustomTaxRate() {
        OrderItem item = new OrderItem("Phone", 200.0, 1);
        OrderRequest request = new OrderRequest(List.of(item), 0.2, "customer456");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation ->{
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setOrderId("order456");
            return orderToSave;
        });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("order456", response.getOrderId());
        assertEquals("customer456", response.getCustomerId());
        assertEquals(200.0, response.getSubtotal());
        assertEquals(40.0, response.getTaxAmount());
        assertEquals(240.0, response.getTotalAmount());
        assertEquals("PENDING_PAYMENT", response.getOrderStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProducer, times(1)).sendOrderEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void createOrder_MultipleItems_CalculatesTotals()
    {
        List<OrderItem> items = List.of(
                new OrderItem("Item1", 50.0, 2),
                new OrderItem("Item2", 30.0, 1)
        );
        OrderRequest request = new OrderRequest(items, null, "customer789");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation ->{
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setOrderId("order789");
            return orderToSave;
        });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("order789", response.getOrderId());
        assertEquals("customer789", response.getCustomerId());
        assertEquals(130.0, response.getSubtotal());
        assertEquals(13.0, response.getTaxAmount());
        assertEquals(143.0, response.getTotalAmount());
        assertEquals("PENDING_PAYMENT", response.getOrderStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProducer, times(1)).sendOrderEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void createOrder_ZeroItems_ReturnsZeroValues()
    {
        OrderRequest request = new OrderRequest(List.of(), null, "customer000");

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation ->{
            Order orderToSave = invocation.getArgument(0);
            orderToSave.setOrderId("order000");
            return orderToSave;
        });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals("order000", response.getOrderId());
        assertEquals("customer000", response.getCustomerId());
        assertEquals(0.0, response.getSubtotal());
        assertEquals(0.0, response.getTaxAmount());
        assertEquals(0.0, response.getTotalAmount());
        assertEquals("PENDING_PAYMENT", response.getOrderStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderProducer, times(1)).sendOrderEvent(any(OrderPlacedEvent.class));
    }

    @Test
    void getOrder_Success_Found() {
        String orderId = "order123";

        Order mockOrder = new Order(
                List.of(new OrderItem("Laptop", 100.0, 2)),
                orderId,
                null,
                "test-customer",
                0.1
        );
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

        OrderResponse response = orderService.getOrder(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.getOrderId());
        assertEquals("PAID", response.getOrderStatus());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrder_Failure_NotFound() {
        String orderId = "nonexistent-order";

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        try {
            orderService.getOrder(orderId);
        } catch (Exception e) {
            assertEquals("Order not found with ID: " + orderId, e.getMessage());
        }

        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void getOrdersByCustomer_Success_ReturnsPage() {
        String customerId = "cust-1";
        Pageable pageable = PageRequest.of(0, 5);

        List<Order> dbOrders = List.of(
                new Order(List.of(), "order-1", OrderStatus.PAID, customerId, 0.1),
                new Order(List.of(), "order-2", OrderStatus.PENDING_PAYMENT, customerId, 0.1)
        );
        Page<Order> mockPage = new PageImpl<>(dbOrders);

        when(orderRepository.findByCustomerId(customerId, pageable)).thenReturn(mockPage);

        Page<OrderResponse> result = orderService.getOrdersByCustomer(customerId, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("order-1", result.getContent().getFirst().getOrderId());
        assertEquals("PAID", result.getContent().getFirst().getOrderStatus());

        verify(orderRepository, times(1)).findByCustomerId(customerId, pageable);
    }

    @Test
    void getOrdersByCustomer_Failure_ReturnsPage() {
        String customerId = "cust-2";
        Pageable pageable = PageRequest.of(0, 5);

        Page<Order> mockPage = new PageImpl<>(List.of());

        when(orderRepository.findByCustomerId(customerId, pageable)).thenReturn(mockPage);

        Page<OrderResponse> result = orderService.getOrdersByCustomer(customerId, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());

        verify(orderRepository, times(1)).findByCustomerId(customerId, pageable);
    }

}
