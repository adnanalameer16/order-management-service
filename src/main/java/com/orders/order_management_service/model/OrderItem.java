package com.orders.order_management_service.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @NotBlank
    private String productId;
    @Positive
    private double price;
    @Min(1)
    private int quantity;
    public double getSubtotal() {
        return price * quantity;
    }
}