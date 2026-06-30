package com.stayhub.property.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.property.dto.Property.CreatePropertyRequest;
import com.stayhub.property.dto.Property.PropertyResponse;
import com.stayhub.property.dto.Property.UpdatePropertyRequest;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private PropertyService propertyService;

    @Test
    void findById_shouldReturnProperty_whenPropertyExists() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Property property = createTestProperty();
        when(propertyRepository.findById(id)).thenReturn(Optional.of(property));

        // when
        PropertyResponse response = propertyService.findById(id);

        // then
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo(property.getName());
        assertThat(response.ownerId()).isEqualTo(property.getOwnerId());
    }

    @Test
    void findById_shouldThrowResourceNotFoundException_whenPropertyDoesNotExist() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> propertyService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAll_shouldReturnPageOfProperties() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Property property = createTestProperty();
        Page<Property> page = new PageImpl<>(List.of(property), pageable, 1);
        when(propertyRepository.findAll(pageable)).thenReturn(page);

        // when
        Page<PropertyResponse> response = propertyService.findAll(pageable);

        // then
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).name()).isEqualTo(property.getName());
    }

    @Test
    void create_shouldCreateProperty() {
        // given
        UUID ownerId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        CreatePropertyRequest request = new CreatePropertyRequest(
                "Grand Hotel", "Main Street 1", "Warsaw", "Poland", "A nice hotel", ownerId);

        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
            Property savedProperty = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedProperty, "id", UUID.fromString("22222222-2222-2222-2222-222222222222"));
            return savedProperty;
        });

        // when
        PropertyResponse response = propertyService.create(request);

        // then
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.ownerId()).isEqualTo(ownerId);
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void update_shouldUpdateOnlyProvidedFields_whenRequestIsPartial() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Property property = createTestProperty();
        UpdatePropertyRequest request = new UpdatePropertyRequest(null, "New Address", null, null, null);

        when(propertyRepository.findById(id)).thenReturn(Optional.of(property));
        when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PropertyResponse response = propertyService.update(id, request);

        // then
        assertThat(response.address()).isEqualTo("New Address");
        assertThat(response.name()).isEqualTo(property.getName());
        assertThat(response.city()).isEqualTo(property.getCity());
    }

    @Test
    void delete_shouldDeleteProperty_whenPropertyExists() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Property property = createTestProperty();
        when(propertyRepository.findById(id)).thenReturn(Optional.of(property));

        // when
        propertyService.delete(id);

        // then
        verify(propertyRepository).delete(property);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenPropertyDoesNotExist() {
        // given
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(propertyRepository.findById(id)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> propertyService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(propertyRepository, never()).delete(any(Property.class));
    }

    private Property createTestProperty() {
        Property property = new Property();
        ReflectionTestUtils.setField(property, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
        property.setName("Grand Hotel");
        property.setAddress("Main Street 1");
        property.setCity("Warsaw");
        property.setCountry("Poland");
        property.setDescription("A nice hotel");
        property.setOwnerId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
        return property;
    }
}
