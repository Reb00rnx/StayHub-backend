package com.stayhub.api.service;

import com.stayhub.booking.repository.BookingRepository;
import com.stayhub.property.dto.Property.PropertyResponse;
import com.stayhub.property.dto.Property.PropertySearchCriteria;
import com.stayhub.property.repository.PropertyRepository;
import com.stayhub.property.search.PropertySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.stayhub.property.entity.Property;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertySearchService {

    private final PropertyRepository propertyRepository;
    private final BookingRepository bookingRepository;

    public Page<PropertyResponse> search(PropertySearchCriteria criteria, Pageable pageable) {
        Specification<Property> spec = PropertySpecification.fromCriteria(criteria);

        if (criteria.checkIn() != null && criteria.checkOut() != null) {
            List<UUID> occupiedRoomIds = bookingRepository.findOccupiedRoomIds(
                    criteria.checkIn(), criteria.checkOut());
            spec = spec.and(PropertySpecification.hasRoomNotIn(occupiedRoomIds));
        }

        return propertyRepository.findAll(spec, pageable)
                .map(PropertyResponse::from);
    }
}
