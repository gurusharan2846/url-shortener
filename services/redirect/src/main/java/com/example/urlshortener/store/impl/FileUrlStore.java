//package com.example.urlshortener.store.impl;
//
//import com.example.urlshortener.store.UrlStore;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import jakarta.annotation.PostConstruct;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//public class FileUrlStore implements UrlStore {
//
//    private final Path path = Paths.get("/tmp/url-store.json");
//    private final ObjectMapper mapper = new ObjectMapper();
//    private volatile Map<String, String> cache = Map.of();
//
//    @PostConstruct
//    void init() throws IOException {
//        reloadMappings();
//    }
//
//    @Override
//    public String get(String code) {
//        return cache.get(code);
//    }
//
//    @Scheduled(fixedRate = 5000)
//    public void reloadMappings() throws IOException {
//        if (!Files.exists(path)) return;
//
//        try {
//            Map<String, String> tmp = mapper.readValue(
//                    path.toFile(),
//                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}
//            );
//            Map<String, String> next = java.util.Collections.unmodifiableMap(tmp);
//            // Swap reference in one step (atomic + visible because volatile)
//            if (!next.equals(cache)) {
//                cache = next;
//                System.out.println("Reloaded mappings: " + cache.size() + " entries");
//            }
//        } catch (IOException e) {
//            System.err.println("Failed to reload mappings: " + e.getMessage());
//        }
//    }
//}
