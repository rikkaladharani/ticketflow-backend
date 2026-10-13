package com.ticketflow.backend.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import com.ticketflow.backend.dto.CheckInRequest;
import com.ticketflow.backend.dto.CheckInResponse;
import com.ticketflow.backend.dto.CustomerTicketResponse;
import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.BookingFoodItem;
import com.ticketflow.backend.entity.BookingSeat;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.EventSeat;
import com.ticketflow.backend.entity.Stall;
import com.ticketflow.backend.entity.StallItem;
import com.ticketflow.backend.entity.Ticket;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.entity.Venue;

import com.ticketflow.backend.repository.BookingFoodItemRepository;
import com.ticketflow.backend.repository.BookingRepository;
import com.ticketflow.backend.repository.BookingSeatRepository;
import com.ticketflow.backend.repository.TicketRepository;
import com.ticketflow.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingFoodItemRepository bookingFoodItemRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final NotificationService notificationService;

    public TicketService(
            TicketRepository ticketRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            BookingFoodItemRepository bookingFoodItemRepository,
            UserRepository userRepository,
            ActivityLogService activityLogService,
            NotificationService notificationService) {

        this.ticketRepository = ticketRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.bookingFoodItemRepository = bookingFoodItemRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
        this.notificationService = notificationService;
    }

    // =========================================================
    // CREATE TICKET
    // =========================================================

    public Ticket createTicket(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RuntimeException(
                    "Ticket can only be generated for a confirmed booking");
        }

        Ticket existingTicket = ticketRepository
                .findByBooking(booking)
                .orElse(null);

        if (existingTicket != null) {
            return existingTicket;
        }

        String ticketNumber =
                "TKT-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                        .toUpperCase();

        String qrData =
                "TICKETFLOW|" +
                ticketNumber +
                "|BOOKING:" +
                booking.getBookingReference();

        String qrCodeImage = generateQrCodeImage(qrData);

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setStatus("VALID");
        ticket.setCheckedIn(false);
        ticket.setQrCodeData(qrData);
        ticket.setQrCodeImage(qrCodeImage);
        ticket.setIssuedAt(LocalDateTime.now());
        ticket.setBooking(booking);

        Ticket saved = ticketRepository.save(ticket);

        if (booking.getCustomer() != null) {
            notificationService.createNotification(
                    booking.getCustomer(),
                    "Ticket Issued Successfully",
                    "Your digital QR ticket " + ticketNumber + " for " + booking.getEvent().getTitle() + " is ready!",
                    "BOOKING"
            );
        }

        return saved;
    }

    // =========================================================
    // QR CODE GENERATION
    // =========================================================

    private String generateQrCodeImage(String data) {

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(
                    bitMatrix,
                    "PNG",
                    outputStream
            );

            String base64Image =
                    Base64.getEncoder()
                            .encodeToString(
                                    outputStream.toByteArray()
                            );

            return "data:image/png;base64," + base64Image;

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to generate QR code",
                    e
            );
        }
    }

    // =========================================================
    // GET TICKET BY ID
    // =========================================================

    public Ticket getTicketById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));
    }

    // =========================================================
    // GET TICKET BY BOOKING
    // =========================================================

    public Ticket getTicketByBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));

        return ticketRepository.findByBooking(booking)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ticket not found for this booking"));
    }

    // =========================================================
    // GET TICKET BY TICKET NUMBER
    // =========================================================

    public Ticket getTicketByNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found"));
    }

    // =========================================================
    // QR CHECK-IN SCANNER / GATE VALIDATION
    // =========================================================

    public CheckInResponse checkInTicket(CheckInRequest request, User staffUser) {
        CheckInResponse response = new CheckInResponse();

        if (request == null || request.getCode() == null || request.getCode().trim().isEmpty()) {
            response.setSuccess(false);
            response.setStatus("INVALID");
            response.setMessage("Scan code or ticket number is required");
            return response;
        }

        String rawCode = request.getCode().trim();
        String parsedTicketNumber = rawCode;

        // Parse standard QR format: TICKETFLOW|TKT-XXXXXXXX|BOOKING:BKG-XXXXXXXX
        if (rawCode.contains("|")) {
            String[] parts = rawCode.split("\\|");
            for (String part : parts) {
                if (part.startsWith("TKT-")) {
                    parsedTicketNumber = part;
                    break;
                }
            }
        }

        Ticket ticket = ticketRepository.findByTicketNumber(parsedTicketNumber).orElse(null);

        if (ticket == null) {
            // Also attempt lookup by exact qrCodeData
            List<Ticket> allTickets = ticketRepository.findAll();
            for (Ticket t : allTickets) {
                if (rawCode.equals(t.getQrCodeData())) {
                    ticket = t;
                    break;
                }
            }
        }

        if (ticket == null) {
            response.setSuccess(false);
            response.setStatus("INVALID");
            response.setMessage("Invalid Ticket: No ticket found matching code '" + parsedTicketNumber + "'");
            return response;
        }

        Booking booking = ticket.getBooking();
        Event event = booking != null ? booking.getEvent() : null;
        User customer = booking != null ? booking.getCustomer() : null;

        response.setTicketNumber(ticket.getTicketNumber());
        if (booking != null) {
            response.setBookingReference(booking.getBookingReference());
        }
        if (event != null) {
            response.setEventTitle(event.getTitle());
            if (event.getEventDate() != null) {
                response.setEventDate(event.getEventDate().atStartOfDay());
            }
        }
        if (customer != null) {
            String customerName = (customer.getFirstName() != null ? customer.getFirstName() : "") +
                    " " + (customer.getLastName() != null ? customer.getLastName() : "");
            response.setAttendeeName(customerName.trim().isEmpty() ? customer.getEmail() : customerName.trim());
            response.setAttendeeEmail(customer.getEmail());
        }

        // Get seat info
        List<String> seats = new ArrayList<>();
        if (booking != null) {
            List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking(booking);
            for (BookingSeat bs : bookingSeats) {
                if (bs.getEventSeat() != null && bs.getEventSeat().getSeat() != null) {
                    seats.add(bs.getEventSeat().getSeat().getSeatRow() + bs.getEventSeat().getSeat().getSeatNumber());
                }
            }
        }
        response.setSeatNumbers(seats);

        // Check 1: Is Booking cancelled?
        if (booking != null && "CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            response.setSuccess(false);
            response.setStatus("CANCELLED");
            response.setMessage("Ticket Cancelled: This booking was cancelled and is NOT valid for entry.");
            return response;
        }

        // Check 2: Is ticket already checked in?
        if (Boolean.TRUE.equals(ticket.getCheckedIn())) {
            response.setSuccess(false);
            response.setStatus("ALREADY_CHECKED_IN");
            response.setCheckedInAt(ticket.getCheckedInAt());
            response.setMessage("Warning: Ticket ALREADY CHECKED IN on " + ticket.getCheckedInAt());
            return response;
        }

        // Check 3: Valid and ready to check in!
        ticket.setCheckedIn(true);
        ticket.setCheckedInAt(LocalDateTime.now());
        ticket.setCheckedInBy(staffUser != null ? staffUser.getEmail() : "Staff");
        ticketRepository.save(ticket);

        response.setSuccess(true);
        response.setStatus("VALID");
        response.setCheckedInAt(ticket.getCheckedInAt());
        response.setMessage("Success! Ticket verified. Entry permitted for " + response.getAttendeeName() + ".");

        // Activity log
        activityLogService.logActivity(
                staffUser,
                "TICKET_CHECKED_IN",
                "Checked in ticket " + ticket.getTicketNumber() + " for event " + (event != null ? event.getTitle() : "")
        );

        // Notify customer
        if (customer != null) {
            notificationService.createNotification(
                    customer,
                    "Checked In to " + (event != null ? event.getTitle() : "Event"),
                    "Welcome! Your ticket " + ticket.getTicketNumber() + " was checked in at the entrance. Enjoy your event!",
                    "SYSTEM"
            );
        }

        return response;
    }

    // =========================================================
    // GET ALL TICKETS OF CUSTOMER
    // =========================================================

    public List<CustomerTicketResponse> getCustomerTickets(Long customerId) {

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        List<Booking> bookings =
                bookingRepository.findByCustomerOrderByBookingTimeDesc(
                        customer
                );

        List<CustomerTicketResponse> responseList =
                new ArrayList<>();

        for (Booking booking : bookings) {

            Ticket ticket =
                    ticketRepository.findByBooking(booking)
                            .orElse(null);

            if (ticket == null) {
                continue;
            }

            CustomerTicketResponse response =
                    new CustomerTicketResponse();

            Event event = booking.getEvent();
            Venue venue = event.getVenue();

            // Ticket information
            response.setTicketId(ticket.getId());
            response.setTicketNumber(ticket.getTicketNumber());
            response.setTicketStatus(ticket.getStatus());
            response.setQrCodeData(ticket.getQrCodeData());
            response.setQrCodeImage(ticket.getQrCodeImage());
            response.setIssuedAt(ticket.getIssuedAt());
            response.setCheckedIn(ticket.getCheckedIn());
            response.setCheckedInAt(ticket.getCheckedInAt());

            // Booking information
            response.setBookingId(booking.getId());
            response.setBookingReference(booking.getBookingReference());
            response.setBookingStatus(booking.getStatus());
            response.setSubtotalAmount(booking.getSubtotalAmount());
            response.setFoodSubtotalAmount(booking.getFoodSubtotalAmount());
            response.setDiscountAmount(booking.getDiscountAmount());
            response.setCouponCode(booking.getCouponCode());
            response.setTotalAmount(booking.getTotalAmount());
            response.setBookingTime(booking.getBookingTime());
            response.setCancellationReason(booking.getCancellationReason());
            response.setCancelledAt(booking.getCancelledAt());

            // Event information
            response.setEventId(event.getId());
            response.setEventTitle(event.getTitle());
            response.setEventDescription(event.getDescription());
            response.setEventCategory(event.getCategory());
            response.setEventImageUrl(event.getImageUrl());
            response.setEventDate(event.getEventDate());
            response.setStartTime(event.getStartTime());
            response.setEndTime(event.getEndTime());

            // Venue information
            if (venue != null) {
                response.setVenueName(venue.getName());
                response.setVenueAddress(venue.getAddress());
                response.setVenueCity(venue.getCity());
            }

            // Seats
            List<BookingSeat> bookingSeats =
                    bookingSeatRepository.findByBooking(booking);

            List<CustomerTicketResponse.SeatInfo> seats =
                    new ArrayList<>();

            for (BookingSeat bookingSeat : bookingSeats) {
                if (bookingSeat == null || bookingSeat.getEventSeat() == null) {
                    continue;
                }
                EventSeat eventSeat = bookingSeat.getEventSeat();
                seats.add(
                        new CustomerTicketResponse.SeatInfo(
                                eventSeat.getId(),
                                eventSeat.getSeat().getSeatRow(),
                                eventSeat.getSeat().getSeatNumber(),
                                eventSeat.getSeat().getSeatType(),
                                eventSeat.getPrice()
                        )
                );
            }
            response.setSeats(seats);

            // Food Items
            List<BookingFoodItem> bookingFoodItems =
                    bookingFoodItemRepository.findByBooking(booking);

            List<CustomerTicketResponse.FoodInfo> foodItems =
                    new ArrayList<>();

            for (BookingFoodItem bookingFoodItem : bookingFoodItems) {
                if (bookingFoodItem == null || bookingFoodItem.getStallItem() == null) {
                    continue;
                }
                StallItem stallItem = bookingFoodItem.getStallItem();
                Stall stall = stallItem.getStall();
                Long stallId = stall != null ? stall.getId() : null;
                String stallName = stall != null ? stall.getStallName() : null;

                foodItems.add(
                        new CustomerTicketResponse.FoodInfo(
                                stallItem.getId(),
                                stallItem.getItemName(),
                                stallItem.getDescription(),
                                stallItem.getCategory(),
                                bookingFoodItem.getQuantity(),
                                bookingFoodItem.getUnitPrice(),
                                bookingFoodItem.getTotalPrice(),
                                stallId,
                                stallName
                        )
                );
            }
            response.setFoodItems(foodItems);

            responseList.add(response);
        }

        return responseList;
    }
}