package com.orders.order_management_service.controller;

import com.orders.order_management_service.dto.OrderRequest;
import com.orders.order_management_service.dto.OrderResponse;
import com.orders.order_management_service.dto.PaginatedResponse;
import com.orders.order_management_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Order Management", description = "APIs for managing customer orders and order lifecycle")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Operation(
        summary = "Create a new order",
        description = "Creates a new order for a customer and publishes an order.created event to Kafka. Calculates tax and total amount automatically."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Order created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/orders")
    public OrderResponse createOrder(
        @Parameter(description = "Order details including customer ID and order items", required = true)
        @Valid @RequestBody OrderRequest request) {
        logger.info("Received request to create order for customer: {}", request.getCustomerId());
        return orderService.createOrder(request);
    }

    @Operation(
        summary = "Get order by ID",
        description = "Retrieves detailed information about a specific order by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderResponse.class))),
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/orders/{id}")
    public OrderResponse getOrder(
        @Parameter(description = "Unique identifier of the order", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String id) {
        logger.info("Received request to fetch order with ID: {}", id);
        return orderService.getOrder(id);
    }

    @Operation(
        summary = "Get orders by customer",
        description = "Retrieves a paginated list of all orders for a specific customer, sorted by creation date"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaginatedResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/customers/{customerId}/orders")
    public PaginatedResponse<OrderResponse> getOrdersByCustomer(
        @Parameter(description = "Unique identifier of the customer", required = true, example = "cust-001")
        @PathVariable String customerId,
        @Parameter(description = "Pagination parameters (page, size, sort)", required = false)
        @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        logger.info("Received request to fetch orders for customer: {}, page: {}, size: {}",
                customerId, pageable.getPageNumber(), pageable.getPageSize());
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