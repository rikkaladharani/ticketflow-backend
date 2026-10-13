package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.VenueSectionRequest;
import com.ticketflow.backend.entity.VenueSection;
import com.ticketflow.backend.service.VenueSectionService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venue-sections")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class VenueSectionController {

    private final VenueSectionService venueSectionService;

    public VenueSectionController(
            VenueSectionService venueSectionService) {

        this.venueSectionService = venueSectionService;
    }

    @PostMapping
    public ResponseEntity<?> createSection(
            @RequestBody VenueSectionRequest request) {

        try {

            VenueSection section =
                    venueSectionService.createSection(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(section);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<VenueSection>> getAllSections() {

        return ResponseEntity.ok(
                venueSectionService.getAllSections()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSection(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    venueSectionService.getSectionById(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<?> getSectionsByVenue(
            @PathVariable Long venueId) {

        try {

            return ResponseEntity.ok(
                    venueSectionService.getSectionsByVenue(venueId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/type/{sectionType}")
    public ResponseEntity<List<VenueSection>> getSectionsByType(
            @PathVariable String sectionType) {

        return ResponseEntity.ok(
                venueSectionService.getSectionsByType(sectionType)
        );
    }

    public record ErrorResponse(String message) {
    }
}