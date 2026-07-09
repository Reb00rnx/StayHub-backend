package com.stayhub.booking.service;

import com.stayhub.booking.dto.BookingResponse;
import com.stayhub.booking.dto.CreateBookingRequest;
import com.stayhub.booking.entity.Booking;
import com.stayhub.booking.entity.BookingStatus;
import com.stayhub.booking.entity.BookingStatusHistory;
import com.stayhub.booking.kafka.BookingEventProducer;
import com.stayhub.booking.repository.BookingRepository;
import com.stayhub.common.exception.BookingConflictException;
import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.common.exception.ValidationException;
import com.stayhub.property.entity.Room;
import com.stayhub.property.entity.RoomStatus;
import com.stayhub.property.repository.RoomRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.stayhub.common.event.BookingEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BookingService {

    private final BookingEventProducer bookingEventProducer;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final Counter bookingsCreated;
    private final Counter bookingsConfirmed;
    private final Counter bookingsCancelled;

    public BookingService(BookingEventProducer bookingEventProducer,
                          BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          MeterRegistry meterRegistry) {
        this.bookingEventProducer = bookingEventProducer;
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.bookingsCreated = meterRegistry.counter("bookings.created");
        this.bookingsConfirmed = meterRegistry.counter("bookings.confirmed");
        this.bookingsCancelled = meterRegistry.counter("bookings.cancelled");
    }

    @Transactional(timeout = 10)
    public BookingResponse createBooking(CreateBookingRequest request) {
        log.info("Creating booking for room {} from {} to {}", request.roomId(), request.checkIn(), request.checkOut());
        Room room = roomRepository.findByIdWithLock(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", request.roomId()));
        List<Booking> overlapping= bookingRepository.findOverlappingBookings(request.roomId(),request.checkIn(),request.checkOut());
        if(!overlapping.isEmpty()){
            log.warn("Booking conflict for room {} between {} and {}", request.roomId(), request.checkIn(), request.checkOut());
            throw new BookingConflictException("Room not available for selected dates");
        }
        Booking booking = new Booking();
        booking.setRoomId(request.roomId());
        booking.setGuestId(request.guestId());
        booking.setCheckIn(request.checkIn());
        booking.setCheckOut(request.checkOut());

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        booking.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        booking.setStatus(BookingStatus.PENDING);
        BookingStatusHistory history = new BookingStatusHistory();
        Booking savedBooking = bookingRepository.save(booking);
        history.setBooking(savedBooking);
        history.setStatus(BookingStatus.PENDING);
        history.setChangedAt(Instant.now());
        savedBooking.getStatusHistory().add(history);
        bookingRepository.save(savedBooking);
        log.info("Booking {} created with status PENDING", savedBooking.getId());
        bookingsCreated.increment();


        bookingEventProducer.publish(new BookingEvent(
        savedBooking.getId(), savedBooking.getGuestId(), savedBooking.getRoomId(),
        "PENDING", savedBooking.getCheckIn(), savedBooking.getCheckOut(),
        savedBooking.getTotalPrice(), Instant.now()));
        return  mapToResponse(savedBooking);

    }

    @Transactional(timeout = 10)
    public BookingResponse confirmBooking(UUID bookingId) {
        Booking booking = findBookingEntity(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Cannot confirm booking {} — current status is {}", bookingId, booking.getStatus());
            throw new ValidationException("Only PENDING bookings can be confirmed");
        }
        booking.setStatus(BookingStatus.CONFIRMED);

        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setStatus(BookingStatus.CONFIRMED);
        history.setChangedAt(Instant.now());
        booking.getStatusHistory().add(history);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking {} confirmed", savedBooking.getId());
        bookingsConfirmed.increment();

        bookingEventProducer.publish(new BookingEvent(
        savedBooking.getId(), savedBooking.getGuestId(), savedBooking.getRoomId(),
        "CONFIRMED", savedBooking.getCheckIn(), savedBooking.getCheckOut(),
        savedBooking.getTotalPrice(), Instant.now()));

        return mapToResponse(savedBooking);
}

    @Transactional(timeout = 10)
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = findBookingEntity(bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PENDING) {
            log.warn("Cannot cancel booking {} — current status is {}", bookingId, booking.getStatus());
            throw new ValidationException("Booking cannot be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);

        Room room = roomRepository.findByIdWithLock(booking.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", booking.getRoomId()));
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);

        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setStatus(BookingStatus.CANCELLED);
        history.setChangedAt(Instant.now());
        history.setReason("Cancelled");
        booking.getStatusHistory().add(history);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking {} cancelled, room {} set back to AVAILABLE", savedBooking.getId(), room.getId());
        bookingsCancelled.increment();

        bookingEventProducer.publish(new BookingEvent(
        savedBooking.getId(), savedBooking.getGuestId(), savedBooking.getRoomId(),
        "CANCELLED", savedBooking.getCheckIn(), savedBooking.getCheckOut(),
        savedBooking.getTotalPrice(), Instant.now()));

        return mapToResponse(savedBooking);
    }

    @Transactional(readOnly = true, timeout = 10)
    public BookingResponse findById(UUID id) {
        return mapToResponse(findBookingEntity(id));
    }

    private Booking findBookingEntity(UUID id) {
        return bookingRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    @Transactional(readOnly = true, timeout = 10)
    public List<BookingResponse> findByGuestId(UUID guestId) {
        List<Booking> bookings = bookingRepository.findByGuestId(guestId);
        return bookings.stream().map(this::mapToResponse).toList();
    }

    private BookingResponse mapToResponse(Booking booking) {
    return new BookingResponse(
            booking.getId(),
            booking.getRoomId(),
            booking.getGuestId(),
            booking.getCheckIn(),
            booking.getCheckOut(),
            booking.getStatus(),
            booking.getTotalPrice(),
            booking.getCreatedAt());
}
}
