package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.CheckInRequest;
import com.ticketflow.backend.dto.CheckInResponse;
import com.ticketflow.backend.dto.CustomerTicketResponse;
import com.ticketflow.backend.entity.Ticket;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.UserRepository;
import com.ticketflow.backend.service.TicketService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class TicketController {

    private final TicketService ticketService;
    private final UserRepository userRepository;

    public TicketController(TicketService ticketService, UserRepository userRepository) {
        this.ticketService = ticketService;
        this.userRepository = userRepository;
    }

    // =========================================================
    // CREATE TICKET
    // =========================================================

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<Ticket> createTicket(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                ticketService.createTicket(bookingId)
        );
    }

    // =========================================================
    // GET TICKET BY ID
    // =========================================================

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> getTicketById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ticketService.getTicketById(id)
        );
    }

    // =========================================================
    // GET TICKET BY BOOKING
    // =========================================================

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<Ticket> getTicketByBooking(
            @PathVariable Long bookingId) {

        return ResponseEntity.ok(
                ticketService.getTicketByBooking(bookingId)
        );
    }

    // =========================================================
    // GET TICKET BY TICKET NUMBER
    // =========================================================

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<Ticket> getTicketByNumber(
            @PathVariable String ticketNumber) {

        return ResponseEntity.ok(
                ticketService.getTicketByNumber(ticketNumber)
        );
    }

    // =========================================================
    // GET ALL TICKETS OF CUSTOMER
    // =========================================================

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerTicketResponse>> getCustomerTickets(
            @PathVariable Long customerId) {

        return ResponseEntity.ok(
                ticketService.getCustomerTickets(customerId)
        );
    }

    // =========================================================
    // QR CHECK-IN SCANNER / GATE VALIDATION
    // =========================================================

    @PostMapping("/check-in")
    public ResponseEntity<CheckInResponse> checkInTicket(
            @RequestBody CheckInRequest request,
            Authentication authentication) {

        User staffUser = null;
        if (authentication != null && authentication.isAuthenticated()) {
            staffUser = userRepository.findByEmail(authentication.getName()).orElse(null);
        }

        return ResponseEntity.ok(
                ticketService.checkInTicket(request, staffUser)
        );
    }
}