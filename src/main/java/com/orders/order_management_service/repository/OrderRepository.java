package com.orders.order_management_service.repository;

import com.orders.order_management_service.model.Order;
import org.springframework.data.aerospike.repository.AerospikeRepository;

public interface OrderRepository extends AerospikeRepository<Order, String> {
}
