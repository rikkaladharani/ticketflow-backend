package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.VenueRequest;
import com.ticketflow.backend.entity.Venue;
import com.ticketflow.backend.service.VenueService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<?> createVenue(
            @RequestBody VenueRequest request) {

        try {

            Venue venue = venueService.createVenue(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(venue);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Venue>> getAllVenues() {

        return ResponseEntity.ok(
                venueService.getAllVenues()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVenue(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    venueService.getVenueById(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<Venue>> getVenuesByCity(
            @PathVariable String city) {

        return ResponseEntity.ok(
                venueService.getVenuesByCity(city)
        );
    }

    @GetMapping("/layout/{layoutType}")
    public ResponseEntity<List<Venue>> getVenuesByLayoutType(
            @PathVariable String layoutType) {

        return ResponseEntity.ok(
                venueService.getVenuesByLayoutType(layoutType)
        );
    }

    public record ErrorResponse(String message) {
    }
}