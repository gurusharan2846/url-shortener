package com.example.urlshortener.controller;

import com.example.urlshortener.store.impl.FileUrlStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final FileUrlStore store;

    public RedirectController(FileUrlStore store) {
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
