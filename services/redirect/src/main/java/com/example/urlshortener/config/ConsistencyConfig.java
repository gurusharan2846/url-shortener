package com.example.urlshortener.config;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsistencyConfig {

    @Bean
    public ConsistencyLevels consistencyLevels(
            @Value("${app.cassandra.writeConsistency:LOCAL_QUORUM}") String writeCl,
            @Value("${app.cassandra.readConsistency:LOCAL_ONE}") String readCl
    ) {
        return new ConsistencyLevels(
                parseCl(writeCl, DefaultConsistencyLevel.LOCAL_ONE),
                parseCl(readCl, DefaultConsistencyLevel.LOCAL_ONE)
        );
    }

    private static ConsistencyLevel parseCl(String s, DefaultConsistencyLevel fallback) {
        if (s == null || s.trim().isEmpty()) return fallback;
        return DefaultConsistencyLevel.valueOf(s.trim().toUpperCase());
    }

    public static final class ConsistencyLevels {
        public final ConsistencyLevel write;
        public final ConsistencyLevel read;
        public ConsistencyLevels(ConsistencyLevel write, ConsistencyLevel read) {
            this.write = write;
            this.read = read;
        }
    }
}
