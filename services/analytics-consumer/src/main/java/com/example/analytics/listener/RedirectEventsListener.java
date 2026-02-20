package com.example.analytics.listener;


import com.example.analytics.model.RedirectEvent;
import com.example.analytics.repository.HourlyClicksRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class RedirectEventsListener {
    private final ObjectMapper om;
    private final HourlyClicksRepository repo;

    public RedirectEventsListener(ObjectMapper om, HourlyClicksRepository repo) {
        this.om = om;
        this.repo = repo;
    }

    @KafkaListener(topics = "${app.kafka.topic}")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        try {
            // value is JSON payload, key is shortCode (optional)
            RedirectEvent ev = om.readValue(record.value(), RedirectEvent.class);

            Instant hourBucket = Instant.ofEpochMilli(ev.ts())
                    .truncatedTo(ChronoUnit.HOURS);

            repo.increment(ev.shortCode(), hourBucket);

            // Commit offset only after successful DB write
            ack.acknowledge();
        } catch (Exception e) {
            // Don't ack => message will be retried
            // (In prod you’d add DLQ for poison messages)
            System.err.println("Failed to process record topic=" + record.topic() +
                    " partition=" + record.partition() +
                    " offset=" + record.offset() +
                    " err=" + e.getMessage());
        }
    }
}
