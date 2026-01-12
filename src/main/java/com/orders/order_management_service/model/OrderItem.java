package com.orders.order_management_service.model;

import lombok.Data;

@Data
public class OrderItem {

    private String productId;
    private double price;
    private int quantity;
    public OrderItem(String productId, double price, int quantity) {

        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return price * quantity;
    }
}