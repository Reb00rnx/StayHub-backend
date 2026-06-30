package com.stayhub.property.controller;

import com.stayhub.common.dto.PageResponse;
import com.stayhub.property.dto.Property.CreatePropertyRequest;
import com.stayhub.property.dto.Property.PropertyResponse;
import com.stayhub.property.dto.Property.UpdatePropertyRequest;
import com.stayhub.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<PageResponse<PropertyResponse>> findAll(Pageable pageable) {
        Page<PropertyResponse> page = propertyService.findAll(pageable);
        PageResponse<PropertyResponse> response = new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> findById(@PathVariable UUID id) {
        PropertyResponse response = propertyService.findById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@RequestBody CreatePropertyRequest request) {
        PropertyResponse response = propertyService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdatePropertyRequest updates) {
        PropertyResponse response = propertyService.update(id,updates);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        propertyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
