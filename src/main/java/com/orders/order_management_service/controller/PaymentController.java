package com.orders.order_management_service.controller;

import com.orders.order_management_service.dto.PaymentRequest;
import com.orders.order_management_service.event.PaymentCompletedEvent;
import com.orders.order_management_service.exception.ResourceNotFoundException;
import com.orders.order_management_service.producer.PaymentProducer;
import com.orders.order_management_service.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Payment Processing", description = "APIs for handling payment transactions and order status updates")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentProducer paymentProducer;

    @Autowired
    private OrderRepository orderRepository;

    @Operation(
        summary = "Process payment for an order",
        description = "Validates the order exists, processes the payment, and publishes a payment.completed event to Kafka. " +
                      "This triggers an asynchronous workflow that updates the order status to PAID and emits an order.ready_for_shipping event."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment processed successfully",
            content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class, example = "Payment Processed"))),
        @ApiResponse(responseCode = "404", description = "Order not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @PostMapping("/payments/complete")
    public String processPayment(
        @Parameter(description = "Payment details including order ID and payment ID", required = true)
        @RequestBody PaymentRequest request) {
        logger.info("Received payment request for Order ID: {}", request.getOrderId());

        if (!orderRepository.existsById(request.getOrderId())) {
            throw new ResourceNotFoundException("Order not found with ID: " + request.getOrderId());
        }

        PaymentCompletedEvent paymentCompletedEvent = new PaymentCompletedEvent(
            request.getOrderId(),
            request.getPaymentId(),
            MDC.get("correlationId")
        );

        paymentProducer.sendPaymentEvent(paymentCompletedEvent);
        logger.info("Payment event published for Order ID: {}", request.getOrderId());
        return "Payment Processed";
    }
}
