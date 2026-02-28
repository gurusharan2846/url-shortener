package com.example.urlshortener.controller;

import com.example.urlshortener.service.CodeGenerator;
import com.example.urlshortener.store.UrlStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class ShortenController {

    private final UrlStore store;
    private final CodeGenerator codeGen;

    private static final Logger log = LoggerFactory.getLogger(ShortenController.class);

    public ShortenController(UrlStore store, CodeGenerator codeGen) {
        this.store = store;
        this.codeGen = codeGen;
    }

    @PostMapping("/shorten")
    public ResponseEntity<String> shortenUrl(@RequestBody Map<String, String> request) {
        log.info("shorten called");
        String longUrl = request.get("url");
        if (longUrl == null || longUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing 'url'");
        }

        for (int attempt = 0; attempt < 10_000; attempt++) {
            for (int len = codeGen.minLen(); len <= codeGen.maxLen(); len++) {
                String code = codeGen.candidateCode(longUrl, attempt, len);

                // 1) Atomically claim the code in Cassandra
                if (store.putIfAbsent(code, longUrl)) {
                    return ResponseEntity.ok(code);
                }

                // 2) Not claimed -> either idempotent or collision
                String existing = store.get(code);
                if (longUrl.equals(existing)) {
                    return ResponseEntity.ok(code); // idempotent
                }
                // else collision -> try longer, then next attempt
            }
        }

        return ResponseEntity.status(500).body("Unable to allocate code");
    }
}

/* Old 1*/
//@RestController
//public class ShortenController {
//
//    private final UrlStore store;
//    private final CodeGenerator codeGen;
//
//    public ShortenController(UrlStore store, CodeGenerator codeGen) {
//        this.store = store;
//        this.codeGen = codeGen;
//    }
//
//    @PostMapping("/shorten")
//    public ResponseEntity<String> shortenUrl(@RequestBody Map<String, String> request) {
//        String longUrl = request.get("url");
//        if (longUrl == null || longUrl.isEmpty()) {
//            return ResponseEntity.badRequest().body("Missing 'url'");
//        }
//
//        // IMPORTANT: atomic check+put to avoid races (two requests same time)
//        synchronized (store) {
//            String code = codeGen.generateCodeForUrl(longUrl);
//
//            String existing = store.get(code);
//            if (existing == null) {
//                store.putIfAbsent(code, longUrl);
//            } else if (!existing.equals(longUrl)) {
//                // Should be rare due to collision handling, but safe guard
//                return ResponseEntity.status(409).body("Collision detected; retry");
//            }
//            return ResponseEntity.ok(code);
//        }
//    }
//}


