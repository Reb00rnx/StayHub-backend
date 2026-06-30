package com.stayhub.property.service;

import com.stayhub.common.exception.ResourceNotFoundException;
import com.stayhub.property.dto.Property.CreatePropertyRequest;
import com.stayhub.property.dto.Property.PropertyResponse;
import com.stayhub.property.dto.Property.UpdatePropertyRequest;
import com.stayhub.property.entity.Property;
import com.stayhub.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;

    @Transactional(readOnly = true, timeout = 10)
    public PropertyResponse findById(UUID id) {
        return mapToResponse(findPropertyEntity(id));
    }

    @Transactional(readOnly = true, timeout = 10)
    public Page<PropertyResponse> findAll(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(timeout = 10)
    public PropertyResponse create(CreatePropertyRequest request) {
        Property property = new Property();
        property.setName(request.name());
        property.setAddress(request.address());
        property.setCity(request.city());
        property.setCountry(request.country());
        property.setDescription(request.description());
        property.setOwnerId(request.ownerId());

        Property savedProperty = propertyRepository.save(property);
        log.info("Property {} created with id {}", savedProperty.getName(), savedProperty.getId());

        return mapToResponse(savedProperty);
    }

    @Transactional(timeout = 10)
    public PropertyResponse update(UUID id, UpdatePropertyRequest updates) {
        Property property = findPropertyEntity(id);

        if (updates.address() != null) {
            log.info("Address changed from {} to {}", property.getAddress(), updates.address());
            property.setAddress(updates.address());
        }
        if (updates.city() != null) {
            log.info("City changed from {} to {}", property.getCity(), updates.city());
            property.setCity(updates.city());
        }
        if (updates.country() != null) {
            log.info("Country changed from {} to {}", property.getCountry(), updates.country());
            property.setCountry(updates.country());
        }
        if (updates.description() != null) {
            log.info("Description changed from {} to {}", property.getDescription(), updates.description());
            property.setDescription(updates.description());
        }
        if (updates.name() != null) {
            log.info("Name changed from {} to {}", property.getName(), updates.name());
            property.setName(updates.name());
        }

        Property savedProperty = propertyRepository.save(property);
        log.info("Property {} updated", savedProperty.getId());

        return mapToResponse(savedProperty);
    }

    @Transactional(timeout = 10)
    public void delete(UUID id) {
        Property property = findPropertyEntity(id);
        propertyRepository.delete(property);
        log.info("Property with id {} was deleted", id);
    }

    private Property findPropertyEntity(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    private PropertyResponse mapToResponse(Property property) {
        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getCity(),
                property.getCountry(),
                property.getDescription(),
                property.getOwnerId(),
                property.getCreatedAt());
    }
}