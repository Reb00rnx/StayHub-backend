package com.stayhub.property.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.common.exception.ValidationException;
import com.stayhub.property.dto.Room.CreateRoomRequest;
import com.stayhub.property.dto.Room.RoomResponse;
import com.stayhub.property.entity.Property;
import com.stayhub.property.entity.Room;
import com.stayhub.property.entity.RoomStatus;
import com.stayhub.property.entity.RoomType;
import com.stayhub.property.repository.PropertyRepository;
import com.stayhub.property.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private RoomService roomService;

    @Test
    void findByPropertyId_shouldReturnRooms_whenPropertyExists() {
        // given
        UUID propertyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Property property = createTestProperty();
        Room room = createTestRoom(property);

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(roomRepository.findByPropertyId(propertyId)).thenReturn(List.of(room));

        // when
        List<RoomResponse> response = roomService.findByPropertyId(propertyId);

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).roomNumber()).isEqualTo(room.getRoomNumber());
    }

    @Test
    void findByPropertyId_shouldThrowResourceNotFoundException_whenPropertyDoesNotExist() {
        // given
        UUID propertyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomService.findByPropertyId(propertyId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roomRepository, never()).findByPropertyId(any());
    }

    @Test
    void findAvailableRooms_shouldReturnAvailableRooms_whenDatesAreValid() {
        // given
        UUID propertyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        LocalDate checkIn = LocalDate.of(2026, 7, 1);
        LocalDate checkOut = LocalDate.of(2026, 7, 5);
        Room room = createTestRoom(createTestProperty());

        when(roomRepository.findAvailableRooms(propertyId, checkIn, checkOut)).thenReturn(List.of(room));

        // when
        List<RoomResponse> response = roomService.findAvailableRooms(propertyId, checkIn, checkOut);

        // then
        assertThat(response).hasSize(1);
        verify(roomRepository).findAvailableRooms(propertyId, checkIn, checkOut);
    }

    @Test
    void findAvailableRooms_shouldThrowValidationException_whenCheckInIsAfterCheckOut() {
        // given
        UUID propertyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        LocalDate checkIn = LocalDate.of(2026, 7, 10);
        LocalDate checkOut = LocalDate.of(2026, 7, 5);

        // when / then
        assertThatThrownBy(() -> roomService.findAvailableRooms(propertyId, checkIn, checkOut))
                .isInstanceOf(ValidationException.class);

        verify(roomRepository, never()).findAvailableRooms(any(), any(), any());
    }

    @Test
    void create_shouldCreateRoom_whenPropertyExists() {
        // given
        UUID propertyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        Property property = createTestProperty();
        CreateRoomRequest request = new CreateRoomRequest("101", RoomType.DOUBLE, BigDecimal.valueOf(150), 2);

        when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room savedRoom = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedRoom, "id", UUID.fromString("44444444-4444-4444-4444-444444444444"));
            return savedRoom;
        });

        // when
        RoomResponse response = roomService.create(propertyId, request);

        // then
        assertThat(response.roomNumber()).isEqualTo("101");
        assertThat(response.status()).isEqualTo(RoomStatus.AVAILABLE);
        assertThat(response.propertyId()).isEqualTo(propertyId);
    }

    @Test
    void create_shouldThrowResourceNotFoundException_whenPropertyDoesNotExist() {
        // given
        UUID propertyId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        CreateRoomRequest request = new CreateRoomRequest("101", RoomType.DOUBLE, BigDecimal.valueOf(150), 2);
        when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomService.create(propertyId, request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void updateStatus_shouldUpdateStatus_whenRoomExists() {
        // given
        UUID roomId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        Room room = createTestRoom(createTestProperty());

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        RoomResponse response = roomService.updateStatus(roomId, RoomStatus.MAINTENANCE);

        // then
        assertThat(response.status()).isEqualTo(RoomStatus.MAINTENANCE);
    }

    @Test
    void updateStatus_shouldThrowResourceNotFoundException_whenRoomDoesNotExist() {
        // given
        UUID roomId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> roomService.updateStatus(roomId, RoomStatus.MAINTENANCE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Property createTestProperty() {
        Property property = new Property();
        ReflectionTestUtils.setField(property, "id", UUID.fromString("33333333-3333-3333-3333-333333333333"));
        property.setName("Grand Hotel");
        property.setAddress("Main Street 1");
        property.setCity("Warsaw");
        property.setCountry("Poland");
        property.setOwnerId(UUID.fromString("55555555-5555-5555-5555-555555555555"));
        return property;
    }

    private Room createTestRoom(Property property) {
        Room room = new Room();
        ReflectionTestUtils.setField(room, "id", UUID.fromString("44444444-4444-4444-4444-444444444444"));
        room.setProperty(property);
        room.setRoomNumber("101");
        room.setType(RoomType.DOUBLE);
        room.setPricePerNight(BigDecimal.valueOf(150));
        room.setMaxGuests(2);
        room.setStatus(RoomStatus.AVAILABLE);
        return room;
    }
}
