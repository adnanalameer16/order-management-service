package com.orders.order_management_service.dto;

import com.orders.order_management_service.model.OrderItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    @NotEmpty
    @Valid
    private List<OrderItem> orderItems;

    private Double taxRate;

    private String customerId;
}
