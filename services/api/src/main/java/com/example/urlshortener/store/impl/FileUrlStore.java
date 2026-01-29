package com.example.urlshortener.store.impl;

import com.example.urlshortener.store.UrlStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
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
            Map<String, String> tmp = mapper.readValue(
                    path.toFile(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
            );
            // IMPORTANT: keep it concurrent (your code expects thread-safe map operations)
            cache = new ConcurrentHashMap<>(tmp);
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
            // snapshot because cache can change while Jackson is serializing it
            Map<String, String> snapshot = new HashMap<>(cache);
            Path tmp = Paths.get(path.toString() + ".tmp");
            mapper.writeValue(tmp.toFile(), snapshot);

            Files.move(
                    tmp,
                    path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
