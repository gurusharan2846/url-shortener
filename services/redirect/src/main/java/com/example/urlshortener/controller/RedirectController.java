package com.example.urlshortener.controller;

import com.example.urlshortener.analytics.RedirectAnalyticsProducer;
import com.example.urlshortener.analytics.RedirectEvent;
import com.example.urlshortener.service.CachedRedirectLookup;
import com.example.urlshortener.store.UrlStore;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final CachedRedirectLookup lookup;
    private final RedirectAnalyticsProducer analyticsProducer;

    public RedirectController(CachedRedirectLookup lookup, RedirectAnalyticsProducer analyticsProducer) {
        this.lookup = lookup;
        this.analyticsProducer = analyticsProducer;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        String longUrl = lookup.resolveLongUrl(code);
        if (longUrl == null) return ResponseEntity.notFound().build();

        analyticsProducer.publish(new RedirectEvent(
                code,
                System.currentTimeMillis(),
                request.getHeader("User-Agent"),
                clientIp(request)
        ));

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }

    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

//@RestController
//public class RedirectController {
//
//    private final UrlStore store;
//
//    public RedirectController(UrlStore store) {
//        this.store = store;
//    }
//
//    @GetMapping("/{code}")
//    public ResponseEntity<Void> redirect(@PathVariable String code) {
//        String longUrl = store.get(code);
//        if (longUrl == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        return ResponseEntity.status(302)
//                .location(URI.create(longUrl))
//                .build();
//    }
//
//}
