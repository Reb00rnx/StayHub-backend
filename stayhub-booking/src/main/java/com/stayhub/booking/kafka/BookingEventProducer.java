package com.stayhub.booking.kafka;

import com.stayhub.common.event.BookingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, BookingEvent> kafkaTemplate;
    private final String topic;

    public BookingEventProducer(KafkaTemplate<String, BookingEvent> kafkaTemplate,
                                @Value("${kafka.topics.booking-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(BookingEvent event) {
        kafkaTemplate.send(topic, event.bookingId().toString(), event);
        log.info("Published booking event: bookingId={}, status={}", event.bookingId(), event.status());
    }
}
