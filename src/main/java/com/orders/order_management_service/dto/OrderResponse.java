package com.orders.order_management_service.dto;

import com.orders.order_management_service.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String orderStatus;
    private String customerId;
    private double subtotal;
    private double taxRate;
    private double taxAmount;
    private double totalAmount;
    private String createdAt;

    private List<OrderItem> items;
}
