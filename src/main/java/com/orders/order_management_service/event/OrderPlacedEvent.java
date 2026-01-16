package com.orders.order_management_service.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {

    private String eventType;
    private String orderId;
    private String timestamp;
    private String customerId;
    private double subtotal;
    private double taxAmount;
    private double totalAmount;
    private String correlationId;
}
