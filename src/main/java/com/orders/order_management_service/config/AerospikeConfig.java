package com.orders.order_management_service.config;

import com.aerospike.client.Host;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.aerospike.config.AbstractAerospikeDataConfiguration;
import org.springframework.data.aerospike.repository.config.EnableAerospikeRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableAerospikeRepositories(basePackages = "com.orders.order_management_service.repository")
public class AerospikeConfig extends AbstractAerospikeDataConfiguration {

    @Value("${spring.aerospike.hosts}")
    private String hosts;

    @Value("${spring.aerospike.namespace}")
    private String namespace;

    @Override
    protected Collection<Host> getHosts() {
        String[] hostParts = hosts.split(":");
        return Collections.singleton(new Host(hostParts[0], Integer.parseInt(hostParts[1])));
    }

    @Override
    protected String nameSpace() {
        return namespace;
    }
}