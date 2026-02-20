package com.example.urlshortener.analytics;

public record RedirectEvent(
        String shortCode,
        long ts,
        String userAgent,
        String ip
) {}
