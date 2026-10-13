package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.EventRequest;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.service.EventService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody EventRequest request) {

        try {

            Event event = eventService.createEvent(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(event);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {

        return ResponseEntity.ok(
                eventService.getAllEvents()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    eventService.getEventById(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<?> getOrganizerEvents(
            @PathVariable Long organizerId) {

        try {

            return ResponseEntity.ok(
                    eventService.getEventsByOrganizer(organizerId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Event>> getEventsByCategory(
            @PathVariable String category) {

        return ResponseEntity.ok(
                eventService.getEventsByCategory(category)
        );
    }

    public record ErrorResponse(String message) {
    }
    
@PutMapping("/{id}/approve")
public ResponseEntity<?> approveEvent(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(eventService.approveEvent(id));
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }
}

@PutMapping("/{id}/reject")
public ResponseEntity<?> rejectEvent(@PathVariable Long id) {
    try {
        return ResponseEntity.ok(eventService.rejectEvent(id));
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
    }
}

}