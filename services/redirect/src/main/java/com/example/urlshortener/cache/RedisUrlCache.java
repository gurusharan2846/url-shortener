package com.example.urlshortener.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class RedisUrlCache {

    private final StringRedisTemplate redis;
    private final long ttlSeconds;;
    private final long jitterSeconds;
    private final String prefix;

    public RedisUrlCache(
            StringRedisTemplate redis,
            @Value("${app.cache.ttlSeconds:3600}") long ttlSeconds,
            @Value("${app.cache.ttlJitterSeconds:0}") long jitterSeconds,
            @Value("${app.cache.prefix:url:}") String prefix) {
        this.redis = redis;
        this.ttlSeconds = ttlSeconds;
        this.jitterSeconds = Math.max(0, jitterSeconds);
        this.prefix = prefix;
    }

    private String key(String code) {
        return prefix + code;
    }

    public String get(String code) {
        return redis.opsForValue().get(key(code));
    }

    public void put(String code, String longUrl) {
        long extra = (jitterSeconds == 0) ? 0 : ThreadLocalRandom.current().nextLong(jitterSeconds + 1);
        Duration ttl = Duration.ofSeconds(ttlSeconds + extra);
        redis.opsForValue().set(key(code), longUrl, ttl);
    }
}
