package com.stayhub.api.controller;

import com.stayhub.api.service.PropertySearchService;
import com.stayhub.common.dto.PageResponse;
import com.stayhub.property.dto.Property.PropertyResponse;
import com.stayhub.property.dto.Property.PropertySearchCriteria;
import com.stayhub.property.entity.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertySearchController {

    private final PropertySearchService propertySearchService;

    @GetMapping("/search")
    public ResponseEntity<PageResponse<PropertyResponse>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) LocalDate checkIn,
            @RequestParam(required = false) LocalDate checkOut,
            Pageable pageable) {

        PropertySearchCriteria criteria = new PropertySearchCriteria(
                city, country, minPrice, maxPrice, roomType, checkIn, checkOut);

        Page<PropertyResponse> page = propertySearchService.search(criteria, pageable);

        return ResponseEntity.ok(new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        ));
    }
}
