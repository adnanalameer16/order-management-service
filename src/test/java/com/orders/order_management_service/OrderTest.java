package com.orders.order_management_service;

import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.model.OrderItem;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

        @Test
        void calculateTotalPrice() {


            List<OrderItem> items = List.of(
                    new OrderItem("1", 100.0, 2)
            );

            Order order = new Order(items, "order123", null, "test-customer", 0.1);

            assertEquals(200.0, order.getSubtotal());
        }
}
