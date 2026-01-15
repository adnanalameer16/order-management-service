package com.orders.order_management_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<OrderResponse> {
    private List<OrderResponse> data;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
}
