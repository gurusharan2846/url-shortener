package com.example.urlshortener.controller;

import com.example.urlshortener.store.UrlStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final UrlStore store;

    public RedirectController(UrlStore store) {
        this.store = store;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String longUrl = store.get(code);
        if (longUrl == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(302)
                .location(URI.create(longUrl))
                .build();
    }
}
