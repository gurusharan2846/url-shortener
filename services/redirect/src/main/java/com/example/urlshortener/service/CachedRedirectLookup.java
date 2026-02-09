package com.example.urlshortener.service;

import com.example.urlshortener.cache.RedisUrlCache;
import com.example.urlshortener.store.UrlStore;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.Counter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CachedRedirectLookup {

    private final RedisUrlCache cache;
    private final UrlStore store; // CassandraUrlStore in redirect-service
    private final Counter cacheHit;
    private final Counter cacheMiss;
    private final Counter dbLoadsCoalesced; // optional visibility

    // key -> in-flight load future
    private final ConcurrentHashMap<String, CompletableFuture<String>> inFlight = new ConcurrentHashMap<>();

    public CachedRedirectLookup(RedisUrlCache cache, UrlStore store, MeterRegistry registry) {
        this.cache = cache;
        this.store = store;
        this.cacheHit = registry.counter("redirect_cache_hit_total");
        this.cacheMiss = registry.counter("redirect_cache_miss_total");
        this.dbLoadsCoalesced = registry.counter("redirect_cache_singleflight_wait_total");
    }

    public String resolveLongUrl(String code) {
        // 1) Fast path: Redis hit
        String cached = cache.get(code);
        if (cached != null && !cached.isBlank()) {
            cacheHit.increment();
            return cached;
        }
        cacheMiss.increment();

        // 2) Singleflight: only one thread loads Cassandra for this code
        CompletableFuture<String> myFuture = new CompletableFuture<>();
        CompletableFuture<String> existing = inFlight.putIfAbsent(code, myFuture);

        if (existing != null) {
            // Someone else is already loading this key; wait for it
            dbLoadsCoalesced.increment();
            return existing.join(); // join throws CompletionException if loader failed
        }

        try {
            // I am the loader
            String fromDb = store.get(code);

            if (fromDb != null && !fromDb.isBlank()) {
                cache.put(code, fromDb); // TTL + jitter applied here
            }

            myFuture.complete(fromDb);
            return fromDb;
        } catch (Throwable t) {
            myFuture.completeExceptionally(t);
            throw t;
        } finally {
            // important: remove only if it's still mapped to my future
            inFlight.remove(code, myFuture);
        }
    }
}
