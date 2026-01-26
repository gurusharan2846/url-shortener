package com.example.urlshortener.store.impl;

import com.example.urlshortener.store.UrlStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class FileUrlStore implements UrlStore {

    private final Path path = Paths.get("/tmp/url-store.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    void init() throws IOException {
        if (Files.exists(path)) {
            cache = mapper.readValue(path.toFile(), Map.class);
        }
    }

    @Override
    public synchronized void put(String code, String longUrl) {
        cache.put(code, longUrl);
        flush();
    }

    @Override
    public String get(String code) {
        return cache.get(code);
    }

    private void flush() {
        try {
            mapper.writeValue(path.toFile(), cache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
