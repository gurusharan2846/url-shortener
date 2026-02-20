package com.example.urlshortener.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedirectAnalyticsProducer {

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    private static final Logger log =
            LoggerFactory.getLogger(RedirectAnalyticsProducer.class);

    public RedirectAnalyticsProducer(
            KafkaTemplate<String, String> kafka,
            ObjectMapper mapper,
            @Value("${app.analytics.topic:redirect-events}") String topic) {
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
    }

    /** Non-blocking: never waits for broker ack; errors are logged via callback. */
    public void publish(RedirectEvent event) {
        final String payload;
        try {
            payload = mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            // Don't break redirects due to analytics serialization
            return;
        }

        kafka.send(topic, event.shortCode(), payload)
                .whenComplete((res, ex) -> {
                    // IMPORTANT: do not throw from here
                    if (ex != null) {
                        log.warn(
                                "analytics publish failed [topic={}, key={}]",
                                topic,
                                event.shortCode(),
                                ex
                        );
                    } else {
                        log.debug(
                                "analytics published [topic={}, partition={}, offset={}]",
                                res.getRecordMetadata().topic(),
                                res.getRecordMetadata().partition(),
                                res.getRecordMetadata().offset()
                        );
                    }
                });
    }
}
