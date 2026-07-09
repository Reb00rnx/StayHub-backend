package com.stayhub.booking.service;

import com.stayhub.booking.dto.BookingResponse;
import com.stayhub.booking.dto.CreateBookingRequest;
import com.stayhub.booking.entity.Booking;
import com.stayhub.booking.entity.BookingStatus;
import com.stayhub.booking.kafka.BookingEventProducer;
import com.stayhub.booking.repository.BookingRepository;
import com.stayhub.common.event.BookingEvent;
import com.stayhub.common.exception.BookingConflictException;
import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.common.exception.ValidationException;
import com.stayhub.property.entity.Room;
import com.stayhub.property.entity.RoomStatus;
import com.stayhub.property.entity.RoomType;
import com.stayhub.property.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingEventProducer bookingEventProducer;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_shouldCreateBooking_whenNoOverlapExists() {
        // given
        UUID roomId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID guestId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 4);
        CreateBookingRequest request = new CreateBookingRequest(roomId, guestId, checkIn, checkOut);
        Room room = createTestRoom(roomId);

        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut)).thenReturn(Collections.emptyList());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedBooking, "id", UUID.fromString("66666666-6666-6666-6666-666666666666"));
            return savedBooking;
        });

        // when
        BookingResponse response = bookingService.createBooking(request);

        // then
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
        assertThat(response.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(450));
        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(bookingEventProducer).publish(any(BookingEvent.class));
    }

    @Test
    void createBooking_shouldThrowBookingConflictException_whenOverlapExists() {
        // given
        UUID roomId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID guestId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 4);
        CreateBookingRequest request = new CreateBookingRequest(roomId, guestId, checkIn, checkOut);
        Room room = createTestRoom(roomId);
        Booking existingBooking = createTestBooking(roomId, guestId, BookingStatus.CONFIRMED);

        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.findOverlappingBookings(roomId, checkIn, checkOut))
                .thenReturn(List.of(existingBooking));

        // when / then
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(BookingConflictException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowResourceNotFoundException_whenRoomDoesNotExist() {
        // given
        UUID roomId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID guestId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        CreateBookingRequest request = new CreateBookingRequest(
                roomId, guestId, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 4));

        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void confirmBooking_shouldConfirmBooking_whenStatusIsPending() {
        // given
        UUID bookingId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        Booking booking = createTestBooking(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                BookingStatus.PENDING);
        ReflectionTestUtils.setField(booking, "id", bookingId);

        when(bookingRepository.findByIdWithLock(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        BookingResponse response = bookingService.confirmBooking(bookingId);

        // then
        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
        verify(bookingEventProducer).publish(any(BookingEvent.class));

    }

    @Test
    void confirmBooking_shouldThrowValidationException_whenStatusIsNotPending() {
        // given
        UUID bookingId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        Booking booking = createTestBooking(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                BookingStatus.CONFIRMED);
        ReflectionTestUtils.setField(booking, "id", bookingId);

        when(bookingRepository.findByIdWithLock(bookingId)).thenReturn(Optional.of(booking));

        // when / then
        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId))
                .isInstanceOf(ValidationException.class);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_shouldCancelBookingAndFreeRoom_whenStatusIsConfirmed() {
        // given
        UUID bookingId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        UUID roomId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        Booking booking = createTestBooking(roomId, UUID.fromString("55555555-5555-5555-5555-555555555555"), BookingStatus.CONFIRMED);
        ReflectionTestUtils.setField(booking, "id", bookingId);
        Room room = createTestRoom(roomId);
        room.setStatus(RoomStatus.OCCUPIED);

        when(bookingRepository.findByIdWithLock(bookingId)).thenReturn(Optional.of(booking));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        BookingResponse response = bookingService.cancelBooking(bookingId);

        // then
        assertThat(response.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(room.getStatus()).isEqualTo(RoomStatus.AVAILABLE);
        verify(roomRepository).save(room);
        verify(bookingEventProducer).publish(any(BookingEvent.class));

    }

    @Test
    void cancelBooking_shouldThrowValidationException_whenStatusIsCancelled() {
        // given
        UUID bookingId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        Booking booking = createTestBooking(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                BookingStatus.CANCELLED);
        ReflectionTestUtils.setField(booking, "id", bookingId);

        when(bookingRepository.findByIdWithLock(bookingId)).thenReturn(Optional.of(booking));

        // when / then
        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId))
                .isInstanceOf(ValidationException.class);

        verify(roomRepository, never()).findByIdWithLock(any());
    }

    @Test
    void findById_shouldReturnBooking_whenBookingExists() {
        // given
        UUID bookingId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        Booking booking = createTestBooking(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("55555555-5555-5555-5555-555555555555"),
                BookingStatus.PENDING);
        ReflectionTestUtils.setField(booking, "id", bookingId);

        when(bookingRepository.findByIdWithLock(bookingId)).thenReturn(Optional.of(booking));

        // when
        BookingResponse response = bookingService.findById(bookingId);

        // then
        assertThat(response.id()).isEqualTo(bookingId);
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenBookingDoesNotExist() {
        // given
        UUID bookingId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        when(bookingRepository.findByIdWithLock(bookingId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> bookingService.findById(bookingId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByGuestId_shouldReturnBookings_whenBookingsExist() {
        // given
        UUID guestId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        Booking booking = createTestBooking(
                UUID.fromString("44444444-4444-4444-4444-444444444444"), guestId, BookingStatus.PENDING);

        when(bookingRepository.findByGuestId(guestId)).thenReturn(List.of(booking));

        // when
        List<BookingResponse> response = bookingService.findByGuestId(guestId);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().guestId()).isEqualTo(guestId);
    }

    private Room createTestRoom(UUID roomId) {
        Room room = new Room();
        ReflectionTestUtils.setField(room, "id", roomId);
        room.setRoomNumber("101");
        room.setType(RoomType.DOUBLE);
        room.setPricePerNight(BigDecimal.valueOf(150));
        room.setMaxGuests(2);
        room.setStatus(RoomStatus.AVAILABLE);
        return room;
    }

    private Booking createTestBooking(UUID roomId, UUID guestId, BookingStatus status) {
        Booking booking = new Booking();
        booking.setRoomId(roomId);
        booking.setGuestId(guestId);
        booking.setCheckIn(LocalDate.of(2026, 7, 1));
        booking.setCheckOut(LocalDate.of(2026, 7, 4));
        booking.setStatus(status);
        booking.setTotalPrice(BigDecimal.valueOf(450));
        return booking;
    }
}
