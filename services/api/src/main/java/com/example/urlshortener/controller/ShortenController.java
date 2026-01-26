package com.example.urlshortener.controller;

import com.example.urlshortener.service.CodeGenerator;
import com.example.urlshortener.store.impl.FileUrlStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@RestController
public class ShortenController {

    private final FileUrlStore store;
    private final CodeGenerator codeGen;

    public ShortenController(FileUrlStore store, CodeGenerator codeGen) {
        this.store = store;
        this.codeGen = codeGen;
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody Map<String, String> request) {
        String longUrl = request.get("url");
        if (longUrl == null || longUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'url'");
        }

        String code = codeGen.generateCode();
        store.put(code, longUrl);
        return ResponseEntity.ok(code);
    }

}
