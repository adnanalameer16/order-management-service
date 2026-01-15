package com.orders.order_management_service.repository;

import com.orders.order_management_service.model.Order;
import org.springframework.data.aerospike.repository.AerospikeRepository;

import java.util.List;

public interface OrderRepository extends AerospikeRepository<Order, String> {

    public List<Order> findByCustomerId(String customerId);
}
