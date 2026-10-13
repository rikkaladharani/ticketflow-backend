package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.SeatGenerationRequest;
import com.ticketflow.backend.entity.Seat;
import com.ticketflow.backend.service.SeatService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class SeatController {

    private final SeatService seatService;

    public SeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateSeats(
            @RequestBody SeatGenerationRequest request) {

        try {

            List<Seat> seats =
                    seatService.generateSeats(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(seats);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Seat>> getAllSeats() {

        return ResponseEntity.ok(
                seatService.getAllSeats()
        );
    }

    @GetMapping("/section/{sectionId}")
    public ResponseEntity<?> getSeatsBySection(
            @PathVariable Long sectionId) {

        try {

            return ResponseEntity.ok(
                    seatService.getSeatsBySection(sectionId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/section/{sectionId}/available")
    public ResponseEntity<?> getAvailableSeats(
            @PathVariable Long sectionId) {

        try {

            return ResponseEntity.ok(
                    seatService.getAvailableSeats(sectionId)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    public record ErrorResponse(String message) {
    }
}