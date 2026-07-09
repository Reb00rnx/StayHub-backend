package com.stayhub.api.service;

import com.stayhub.booking.repository.BookingRepository;
import com.stayhub.property.dto.Property.PropertyResponse;
import com.stayhub.property.dto.Property.PropertySearchCriteria;
import com.stayhub.property.entity.Property;
import com.stayhub.property.repository.PropertyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertySearchServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PropertySearchService propertySearchService;

    @Test
    void search_shouldReturnProperties_whenNoCriteriaGiven() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Property property = createTestProperty();
        Page<Property> page = new PageImpl<>(List.of(property));
        PropertySearchCriteria criteria = new PropertySearchCriteria(null, null, null, null, null, null, null);

        when(propertyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // when
        Page<PropertyResponse> result = propertySearchService.search(criteria, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).city()).isEqualTo("Warsaw");
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void search_shouldQueryOccupiedRooms_whenDatesProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate checkIn = LocalDate.of(2026, 8, 1);
        LocalDate checkOut = LocalDate.of(2026, 8, 5);
        PropertySearchCriteria criteria = new PropertySearchCriteria(null, null, null, null, null, checkIn, checkOut);

        UUID occupiedRoomId = UUID.randomUUID();
        when(bookingRepository.findOccupiedRoomIds(checkIn, checkOut)).thenReturn(List.of(occupiedRoomId));
        when(propertyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());

        // when
        propertySearchService.search(criteria, pageable);

        // then
        verify(bookingRepository).findOccupiedRoomIds(checkIn, checkOut);
        verify(propertyRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void search_shouldNotQueryOccupiedRooms_whenOnlyCheckInProvided() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        PropertySearchCriteria criteria = new PropertySearchCriteria(
                null, null, null, null, null, LocalDate.of(2026, 8, 1), null);

        when(propertyRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(Page.empty());

        // when
        propertySearchService.search(criteria, pageable);

        // then
        verifyNoInteractions(bookingRepository);
    }

    private Property createTestProperty() {
        Property property = new Property();
        ReflectionTestUtils.setField(property, "id", UUID.randomUUID());
        property.setName("Grand Hotel");
        property.setAddress("ul. Marszałkowska 1");
        property.setCity("Warsaw");
        property.setCountry("Poland");
        property.setOwnerId(UUID.randomUUID());
        return property;
    }
}
