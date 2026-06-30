package com.stayhub.property.repository;

import com.stayhub.property.entity.Room;
import com.stayhub.property.entity.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByPropertyId(UUID propertyId);

    List<Room> findByPropertyIdAndStatus(UUID propertyId, RoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :id")
    Optional<Room> findByIdWithLock(@Param("id") UUID id);

    @Query(value = """
            SELECT r.* FROM rooms r
            WHERE r.property_id = :propertyId
            AND r.status = 'AVAILABLE'
            AND r.id NOT IN (
                SELECT b.room_id FROM bookings b
                WHERE b.status <> 'CANCELLED'
                AND b.check_in < :checkOut
                AND b.check_out > :checkIn
            )
            """, nativeQuery = true)
    List<Room> findAvailableRooms(@Param("propertyId") UUID propertyId,
                                   @Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut);
}
