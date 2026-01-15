package com.orders.order_management_service.model;

import com.aerospike.client.query.IndexCollectionType;
import com.aerospike.client.query.IndexType;
import lombok.Data;
import org.springframework.data.aerospike.annotation.Indexed;
import org.springframework.data.aerospike.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;


@Data
@Document
public class Order {

    private List<OrderItem> items;
    private OrderStatus orderStatus;
    @Id
    private String orderId;
    @Indexed(type = IndexType.STRING, collectionType = IndexCollectionType.DEFAULT)
    private String customerId;
    private double subtotal;
    private double taxRate;
    private double taxAmount;
    private double totalAmount;
    private String createdAt;

    public Order(List<OrderItem> items, String orderId, OrderStatus orderStatus, String customerId, double taxRate) {
        this.taxRate = taxRate;
        this.customerId = customerId;
        this.createdAt = Instant.now().toString();
        this.orderId = orderId;
        this.items = items;
        this.orderStatus = orderStatus;
        this.calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        subtotal = 0.0;
        if (items != null) {
            for (OrderItem item : items) {
                this.subtotal += item.getSubtotal();
            }
        }
        taxAmount = subtotal * taxRate;
        totalAmount = subtotal + taxAmount;
    }
}
