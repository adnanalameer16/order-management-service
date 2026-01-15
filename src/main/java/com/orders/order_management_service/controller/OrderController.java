package com.orders.order_management_service.controller;

import com.orders.order_management_service.dto.OrderRequest;
import com.orders.order_management_service.dto.OrderResponse;
import com.orders.order_management_service.dto.PaginatedResponse;
import com.orders.order_management_service.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public OrderResponse createOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(@PathVariable String id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/customers/{customerId}/orders")
    public PaginatedResponse<OrderResponse> getOrdersByCustomer(@PathVariable String customerId,
                                                                @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<OrderResponse> orders = orderService.getOrdersByCustomer(customerId, pageable);

        return new PaginatedResponse<>(
                orders.getContent(),
                orders.getNumber(),
                orders.getTotalPages(),
                orders.getTotalElements(),
                orders.getSize()
        );
    }
}