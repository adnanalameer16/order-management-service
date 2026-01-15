package com.orders.order_management_service.repository;

import com.orders.order_management_service.model.Order;
import org.springframework.data.aerospike.repository.AerospikeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository extends AerospikeRepository<Order, String> {

    public Page<Order> findByCustomerId(String customerId, Pageable pageable);
}
