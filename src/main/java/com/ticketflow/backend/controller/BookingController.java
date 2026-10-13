
package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.BookingRequest;
import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.UserRepository;
import com.ticketflow.backend.service.BookingService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(
            BookingService bookingService,
            UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    // CREATE BOOKING
    @PostMapping
    public ResponseEntity<?> createBooking(
            @RequestBody BookingRequest request,
            Authentication authentication) {
        try {
            User loggedInUser = getAuthenticatedUser(authentication);

            if (!"CUSTOMER".equals(loggedInUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(
                                "Only customers can create bookings"));
            }

            if (request.getCustomerId() == null
                    || !loggedInUser.getId()
                            .equals(request.getCustomerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(
                                "You can only create bookings for your own account"));
            }

            Booking booking = bookingService.createBooking(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(booking);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // GET BOOKING BY ID — customer owner or admin
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User loggedInUser = getAuthenticatedUser(authentication);
            Booking booking = bookingService.getBookingById(id);

            boolean isOwner = booking.getCustomer() != null
                    && booking.getCustomer().getId()
                            .equals(loggedInUser.getId());
            boolean isAdmin = "ADMIN".equals(loggedInUser.getRole());

            if (!isOwner && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(
                                "You are not allowed to view this booking"));
            }

            return ResponseEntity.ok(booking);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // GET BOOKINGS FOR AN ORGANIZER'S EVENTS
    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<?> getOrganizerBookings(
            @PathVariable Long organizerId,
            Authentication authentication) {
        try {
            User loggedInUser = getAuthenticatedUser(authentication);

            boolean isAdmin = "ADMIN".equals(loggedInUser.getRole());
            boolean isOwnOrganizerAccount =
                    "ORGANIZER".equals(loggedInUser.getRole())
                    && loggedInUser.getId().equals(organizerId);

            if (!isAdmin && !isOwnOrganizerAccount) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse(
                                "You can only view bookings for your own events"));
            }

            List<Map<String, Object>> bookings =
                    bookingService.getOrganizerBookings(organizerId);

            return ResponseEntity.ok(bookings);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ADMIN BOOKING STATISTICS
    @GetMapping("/admin/statistics")
    public ResponseEntity<Map<String, Object>> getBookingStatistics() {
        return ResponseEntity.ok(
                bookingService.getBookingStatistics());
    }

    // CANCEL BOOKING (CUSTOMER OR ADMIN)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long id,
            @RequestBody(required = false) com.ticketflow.backend.dto.CancellationRequest request,
            Authentication authentication) {
        try {
            User loggedInUser = getAuthenticatedUser(authentication);
            String reason = request != null ? request.getReason() : "Customer cancellation request";
            Booking cancelledBooking = bookingService.cancelBooking(id, reason, loggedInUser);
            return ResponseEntity.ok(cancelledBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ADMIN GET ALL REFUNDS
    @GetMapping("/admin/refunds")
    public ResponseEntity<?> getAllRefunds(Authentication authentication) {
        try {
            User loggedInUser = getAuthenticatedUser(authentication);
            if (!"ADMIN".equals(loggedInUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Only administrators can view refund requests"));
            }
            return ResponseEntity.ok(bookingService.getAllRefunds());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // ADMIN PROCESS REFUND
    @PutMapping("/admin/refunds/{refundId}/process")
    public ResponseEntity<?> processRefund(
            @PathVariable Long refundId,
            Authentication authentication) {
        try {
            User loggedInUser = getAuthenticatedUser(authentication);
            if (!"ADMIN".equals(loggedInUser.getRole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErrorResponse("Only administrators can process refunds"));
            }
            return ResponseEntity.ok(bookingService.processRefund(refundId, loggedInUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Authentication required");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));
    }

    public record ErrorResponse(String message) {
    }
}
