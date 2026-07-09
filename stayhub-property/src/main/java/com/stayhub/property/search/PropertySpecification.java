package com.stayhub.property.search;

import com.stayhub.property.dto.Property.PropertySearchCriteria;
import com.stayhub.property.entity.Property;
import com.stayhub.property.entity.RoomType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PropertySpecification {

    public static Specification<Property> fromCriteria(PropertySearchCriteria criteria) {
        return Specification
            .where(hasCity(criteria.city()))
            .and(hasCountry(criteria.country()))
            .and(hasRoomWithMaxPrice(criteria.maxPrice()))
            .and(hasRoomWithMinPrice(criteria.minPrice()))
            .and(hasRoomOfType(criteria.roomType()));
    }

    private static Specification<Property> hasCity(String city) {
        if (city == null) return null;
        return (root, query, cb) -> cb.equal(root.get("city"), city);
    }

    private static Specification<Property> hasCountry(String country) {
        if (country == null) return null;
        return (root, query, cb) -> cb.equal(root.get("country"), country);
    }

    private static Specification<Property> hasRoomWithMaxPrice(BigDecimal maxPrice) {
        if (maxPrice == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.join("rooms").get("pricePerNight"), maxPrice);
    }

    private static Specification<Property> hasRoomWithMinPrice(BigDecimal minPrice) {
        if (minPrice == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.join("rooms").get("pricePerNight"), minPrice);
    }

    private static Specification<Property> hasRoomOfType(RoomType roomType) {
        if (roomType == null) return null;
        return (root, query, cb) -> cb.equal(root.join("rooms").get("type"), roomType);
    }

    public static Specification<Property> hasRoomNotIn(List<UUID> occupiedRoomIds) {
        if (occupiedRoomIds == null || occupiedRoomIds.isEmpty()) return null;
        return (root, query, cb) -> cb.not(root.join("rooms").get("id").in(occupiedRoomIds));
    }
}
