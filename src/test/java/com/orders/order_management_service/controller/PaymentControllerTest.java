package com.orders.order_management_service.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.orders.order_management_service.dto.PaymentRequest;
import com.orders.order_management_service.producer.PaymentProducer;
import com.orders.order_management_service.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentProducer paymentProducer;

    @MockitoBean
    private OrderRepository orderRepository;

    @Test
    void ProcessPayment() throws Exception {

        String orderId = "order1";

        PaymentRequest paymentRequest = new PaymentRequest(orderId, "payment1");

        given(orderRepository.existsById(orderId)).willReturn(true);

        mockMvc.perform(post("/payments/complete")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment Processed"));
    }
}
