package com.stayhub.notification.service;

import com.stayhub.common.event.BookingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    @KafkaListener(topics = "${kafka.topics.booking-events}",
                   groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingEvent(BookingEvent event) {
        log.info("Received booking event: bookingId={}, status={}, guestId={}",
                event.bookingId(), event.status(), event.guestId());

        switch (event.status()) {
            case "PENDING"   -> log.info("Sending booking confirmation email to guest {}", event.guestId());
            case "CONFIRMED" -> log.info("Sending booking confirmed email to guest {}", event.guestId());
            case "CANCELLED" -> log.info("Sending booking cancelled email to guest {}", event.guestId());
            default          -> log.warn("Unknown booking status: {}", event.status());
        }
    }
}
