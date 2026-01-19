package com.orders.order_management_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orders.order_management_service.dto.OrderRequest;
import com.orders.order_management_service.dto.OrderResponse;
import com.orders.order_management_service.model.OrderItem;
import com.orders.order_management_service.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getOrderById() throws Exception {
        String orderId = "order123";

        OrderResponse mockResponse = new OrderResponse();
        mockResponse.setOrderId(orderId);
        mockResponse.setOrderStatus("PAID");
        mockResponse.setTotalAmount(200.0);

        given(orderService.getOrder(orderId)).willReturn(mockResponse);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.orderStatus").value("PAID"))
                .andExpect(jsonPath("$.totalAmount").value(200.0));
    }

    @Test
    void createOrder() throws Exception {
        OrderItem mockOrderItem = new OrderItem("Laptop",1000.0,1);

        OrderRequest mockOrderRequest = new OrderRequest(List.of(mockOrderItem),0.1,"cust-1");

        OrderResponse mockResponse = new OrderResponse();
        mockResponse.setOrderId("order123");
        mockResponse.setOrderStatus("PENDING_PAYMENT");
        mockResponse.setTotalAmount(1100.0);

        given(orderService.createOrder(mockOrderRequest)).willReturn(mockResponse);

        mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(mockOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order123"))
                .andExpect(jsonPath("$.orderStatus").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(1100.0));
    }

    @Test
    void getOrdersByCustomer() throws Exception {

        String customerId = "cust-1";

        OrderResponse order1 = new OrderResponse();
        order1.setOrderId("order1");
        order1.setTotalAmount(100.0);

        OrderResponse order2 = new OrderResponse();
        order2.setOrderId("order2");
        order2.setTotalAmount(200.0);

        List<OrderResponse> mockList = List.of(order1, order2);

        Page<OrderResponse> mockPage = new PageImpl<>(mockList);

        given(orderService.getOrdersByCustomer(eq(customerId), any(Pageable.class))).willReturn(mockPage);

        mockMvc.perform(get("/customers/{customerId}/orders", customerId)
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].orderId").value("order1"))
                .andExpect(jsonPath("$.data[1].orderId").value("order2"));
    }

}
