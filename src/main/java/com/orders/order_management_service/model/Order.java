package com.orders.order_management_service.model;

import lombok.Data;
import org.springframework.data.aerospike.mapping.Document;
import org.springframework.data.annotation.Id;

import lombok.Getter;

import java.util.List;


@Data
@Document
public class Order {

    @Getter
    private List<OrderItem> items;

    private OrderStatus orderStatus;

    @Id
    private String orderId;

    @Getter     // using Lombok to generate getter for totalPrice (return this.totalPrice alternatively)
    private double totalPrice;

    public Order(List<OrderItem> items, String orderId, OrderStatus orderStatus) {
        this.orderId = orderId;
        this.items = items;
        this.orderStatus = orderStatus;
        this.calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        totalPrice = 0.0;
        for (OrderItem item : items) {
            totalPrice += item.getSubtotal();
        }
    }
}
