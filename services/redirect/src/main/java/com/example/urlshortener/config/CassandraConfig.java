package com.example.urlshortener.config;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Arrays;

@Configuration
public class CassandraConfig {

    @Bean
    public CqlSession cqlSession(
            @Value("${cassandra.contactPoints}") String contactPoints,
            @Value("${cassandra.port}") int port,
            @Value("${cassandra.datacenter}") String datacenter,
            @Value("${cassandra.keyspace}") String keyspace
    ) {
        var b = CqlSession.builder()
                .withLocalDatacenter(datacenter)
                .withKeyspace(keyspace);

        Arrays.stream(contactPoints.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(cp -> b.addContactPoint(new InetSocketAddress(cp, port)));

        return b.build();
    }
}