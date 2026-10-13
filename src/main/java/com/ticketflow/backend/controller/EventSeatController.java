package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.EventSeatRequest;
import com.ticketflow.backend.entity.EventSeat;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.UserRepository;
import com.ticketflow.backend.service.EventSeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/event-seats")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class EventSeatController {

    private final EventSeatService eventSeatService;
    private final UserRepository userRepository;

    public EventSeatController(
            EventSeatService eventSeatService,
            UserRepository userRepository) {
        this.eventSeatService = eventSeatService;
        this.userRepository = userRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateEventSeats(
            @RequestBody EventSeatRequest request) {

        try {
            List<EventSeat> eventSeats = eventSeatService.generateEventSeats(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(eventSeats);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventSeats(
            @PathVariable Long eventId) {

        try {
            return ResponseEntity.ok(eventSeatService.getEventSeats(eventId));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/event/{eventId}/available")
    public ResponseEntity<?> getAvailableEventSeats(
            @PathVariable Long eventId) {

        try {
            return ResponseEntity.ok(eventSeatService.getAvailableEventSeats(eventId));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{eventSeatId}/hold")
    public ResponseEntity<?> holdSeat(
            @PathVariable Long eventSeatId,
            @RequestParam Long customerId,
            Authentication authentication) {

        try {
            User loggedInUser = userRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!"CUSTOMER".equals(loggedInUser.getRole())
                    || !loggedInUser.getId().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(
                                "You can only hold seats for your own account"));
            }

            return ResponseEntity.ok(
                    eventSeatService.holdSeat(eventSeatId, loggedInUser.getId()));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{eventSeatId}/release")
    public ResponseEntity<?> releaseSeat(
            @PathVariable Long eventSeatId,
            @RequestParam Long customerId,
            Authentication authentication) {

        try {
            User loggedInUser = userRepository
                    .findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!"CUSTOMER".equals(loggedInUser.getRole())
                    || !loggedInUser.getId().equals(customerId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(
                                "You can only release your own seat holds"));
            }

            return ResponseEntity.ok(
                    eventSeatService.releaseSeat(eventSeatId, loggedInUser.getId()));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    public record ErrorResponse(String message) {
    }
}
