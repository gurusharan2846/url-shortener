package com.example.analytics.model;

public record RedirectEvent(
        String shortCode,
        long ts,
        String userAgent,
        String ip
) {}
