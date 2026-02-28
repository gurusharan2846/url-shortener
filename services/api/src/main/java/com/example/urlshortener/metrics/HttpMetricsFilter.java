package com.example.urlshortener.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Order(1)
public class HttpMetricsFilter extends OncePerRequestFilter {

    private final MeterRegistry registry;

    public HttpMetricsFilter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        long start = System.nanoTime();
        try {
            chain.doFilter(req, res);
        } finally {
            long duration = System.nanoTime() - start;

            String method = req.getMethod();
            String uri = req.getRequestURI();   // simple; no query params
            String status = Integer.toString(res.getStatus());

            Counter.builder("http_requests_total")
                    .tag("method", method)
                    .tag("uri", uri)
                    .tag("status", status)
                    .register(registry)
                    .increment();

            Timer.builder("http_request_duration_seconds")
                    .tag("method", method)
                    .tag("uri", uri)
                    .tag("status", status)
                    .publishPercentileHistogram(true)  // enables *_bucket series
                    .register(registry)
                    .record(duration, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }
}
