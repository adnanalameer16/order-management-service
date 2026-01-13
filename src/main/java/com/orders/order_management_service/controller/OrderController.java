package com.orders.order_management_service.controller;

import com.orders.order_management_service.model.Order;
import com.orders.order_management_service.service.OrderService;
import com.orders.order_management_service.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public Order createOrder(@RequestBody List<OrderItem> items) {
        return orderService.createOrder(items);
    }

    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderService.getOrder(id);
    }
}
