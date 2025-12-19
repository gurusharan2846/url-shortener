package com.example.urlshortener.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class UrlController {

    // In-memory map: shortCode -> long URL
    private final Map<String, String> urlMap = new ConcurrentHashMap<>();

    // POST /shorten
    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody Map<String, String> request) {
        String longUrl = request.get("url");
        if (longUrl == null || longUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'url' in request");
        }

        // Generate a random 6-char code
        String code = generateCode();
        urlMap.put(code, longUrl);
        return ResponseEntity.ok(code);
    }

    // GET /{code}
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = urlMap.get(code);
        if (longUrl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(302).location(URI.create(longUrl)).build();
    }

    // Simple random 6-character code
    private String generateCode() {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int idx = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(idx));
        }
        return sb.toString();
    }
}
