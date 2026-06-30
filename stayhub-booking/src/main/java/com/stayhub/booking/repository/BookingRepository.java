package com.stayhub.booking.repository;

import com.stayhub.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByGuestId(UUID guestId);

    List<Booking> findByRoomId(UUID roomId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") UUID id);

    @Query("SELECT b FROM Booking b " +
           "WHERE b.roomId = :roomId " +
           "AND b.status <> 'CANCELLED' " +
           "AND b.checkIn < :checkOut " +
           "AND b.checkOut > :checkIn")
    List<Booking> findOverlappingBookings(
            @Param("roomId") UUID roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}
