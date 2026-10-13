package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.BookingFoodItemRequest;
import com.ticketflow.backend.dto.BookingRequest;
import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.BookingFoodItem;
import com.ticketflow.backend.entity.BookingSeat;
import com.ticketflow.backend.entity.Coupon;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.EventSeat;
import com.ticketflow.backend.entity.EventStall;
import com.ticketflow.backend.entity.StallItem;
import com.ticketflow.backend.entity.User;

import com.ticketflow.backend.repository.BookingFoodItemRepository;
import com.ticketflow.backend.repository.BookingRepository;
import com.ticketflow.backend.repository.BookingSeatRepository;
import com.ticketflow.backend.repository.EventRepository;
import com.ticketflow.backend.repository.EventSeatRepository;
import com.ticketflow.backend.repository.EventStallRepository;
import com.ticketflow.backend.repository.StallItemRepository;
import com.ticketflow.backend.repository.TicketRepository;
import com.ticketflow.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import com.ticketflow.backend.entity.Refund;
import com.ticketflow.backend.repository.RefundRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingFoodItemRepository bookingFoodItemRepository;
    private final EventSeatRepository eventSeatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final StallItemRepository stallItemRepository;
    private final EventStallRepository eventStallRepository;
    private final CouponService couponService;
    private final TicketRepository ticketRepository;
    private final RefundRepository refundRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public BookingService(
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            BookingFoodItemRepository bookingFoodItemRepository,
            EventSeatRepository eventSeatRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            StallItemRepository stallItemRepository,
            EventStallRepository eventStallRepository,
            CouponService couponService,
            TicketRepository ticketRepository,
            RefundRepository refundRepository,
            NotificationService notificationService,
            ActivityLogService activityLogService) {

        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.bookingFoodItemRepository = bookingFoodItemRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.stallItemRepository = stallItemRepository;
        this.eventStallRepository = eventStallRepository;
        this.couponService = couponService;
        this.ticketRepository = ticketRepository;
        this.refundRepository = refundRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @Transactional
    public Booking createBooking(BookingRequest request) {

        // ---------------------------------------------------------
        // CUSTOMER
        // ---------------------------------------------------------

        User customer = userRepository.findById(
                request.getCustomerId()
        ).orElseThrow(() ->
                new RuntimeException("Customer not found")
        );

        if (!"CUSTOMER".equals(customer.getRole())) {
            throw new RuntimeException(
                    "Only customers can create bookings"
            );
        }

        // ---------------------------------------------------------
        // EVENT
        // ---------------------------------------------------------

        Event event = eventRepository.findById(
                request.getEventId()
        ).orElseThrow(() ->
                new RuntimeException("Event not found")
        );

        // ---------------------------------------------------------
        // SEATS
        // ---------------------------------------------------------

        if (request.getEventSeatIds() == null ||
                request.getEventSeatIds().isEmpty()) {

            throw new RuntimeException(
                    "At least one seat must be selected"
            );
        }

        List<EventSeat> selectedSeats =
                eventSeatRepository.findAllById(
                        request.getEventSeatIds()
                );

        if (selectedSeats.size() !=
                request.getEventSeatIds().size()) {

            throw new RuntimeException(
                    "One or more seats were not found"
            );
        }

        double ticketSubtotalAmount = 0.0;

        for (EventSeat eventSeat : selectedSeats) {

            if (!eventSeat.getEvent()
                    .getId()
                    .equals(event.getId())) {

                throw new RuntimeException(
                        "Seat does not belong to this event"
                );
            }

            if (!"HELD".equals(eventSeat.getStatus())) {

                throw new RuntimeException(
                        "All selected seats must be held before booking"
                );
            }

            if (eventSeat.getHeldBy() == null ||
                    !eventSeat.getHeldBy()
                            .getId()
                            .equals(customer.getId())) {

                throw new RuntimeException(
                        "You do not hold one or more selected seats"
                );
            }

            if (eventSeat.getHoldExpiresAt() == null ||
                    eventSeat.getHoldExpiresAt()
                            .isBefore(LocalDateTime.now())) {

                throw new RuntimeException(
                        "One or more seat holds have expired"
                );
            }

            ticketSubtotalAmount += eventSeat.getPrice();
        }

        // ---------------------------------------------------------
        // FOOD ITEMS
        // ---------------------------------------------------------

        double foodSubtotalAmount = 0.0;

        List<BookingFoodItemRequest> requestedFoodItems =
                request.getFoodItems();

        if (requestedFoodItems != null) {

            for (BookingFoodItemRequest foodRequest :
                    requestedFoodItems) {

                if (foodRequest == null) {
                    continue;
                }

                if (foodRequest.getStallItemId() == null) {

                    throw new RuntimeException(
                            "Food item ID is required"
                    );
                }

                if (foodRequest.getQuantity() == null ||
                        foodRequest.getQuantity() <= 0) {

                    throw new RuntimeException(
                            "Food quantity must be greater than zero"
                    );
                }

                // -------------------------------------------------
                // FIND FOOD ITEM
                // -------------------------------------------------

                StallItem stallItem =
                        stallItemRepository.findById(
                                foodRequest.getStallItemId()
                        ).orElseThrow(() ->
                                new RuntimeException(
                                        "Food item not found: " +
                                                foodRequest.getStallItemId()
                                )
                        );

                // -------------------------------------------------
                // CHECK AVAILABILITY
                // -------------------------------------------------

                if (!Boolean.TRUE.equals(
                        stallItem.getAvailable())) {

                    throw new RuntimeException(
                            "Food item is currently unavailable: " +
                                    stallItem.getItemName()
                    );
                }

                // -------------------------------------------------
                // CHECK STOCK
                // -------------------------------------------------

                if (foodRequest.getQuantity() >
                        stallItem.getAvailableQuantity()) {

                    throw new RuntimeException(
                            "Not enough stock for: " +
                                    stallItem.getItemName()
                    );
                }

                // -------------------------------------------------
                // CHECK EVENT STALL
                // -------------------------------------------------

                EventStall eventStall =
                        eventStallRepository
                                .findByEventAndStall(
                                        event,
                                        stallItem.getStall()
                                )
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "This food stall is not available " +
                                                        "for the selected event"
                                        )
                                );

                if (!"APPROVED".equals(
                        eventStall.getStatus())) {

                    throw new RuntimeException(
                            "This food stall is not approved " +
                                    "for the selected event"
                    );
                }

                // -------------------------------------------------
                // CALCULATE FOOD PRICE
                // -------------------------------------------------

                double unitPrice =
                        stallItem.getPrice();

                double itemTotal =
                        unitPrice *
                                foodRequest.getQuantity();

                foodSubtotalAmount += itemTotal;
            }
        }

        // =========================================================
        // COMBINED SUBTOTAL
        // =========================================================

        double combinedSubtotal =
                ticketSubtotalAmount +
                        foodSubtotalAmount;

        // =========================================================
        // COUPON
        // =========================================================

        double discountAmount = 0.0;
        String couponCode = null;

        if (request.getCouponCode() != null &&
                !request.getCouponCode()
                        .trim()
                        .isEmpty()) {

            couponCode =
                    request.getCouponCode()
                            .trim()
                            .toUpperCase();

            Coupon coupon =
                    couponService.getValidCoupon(
                            couponCode
                    );

            if (coupon == null) {

                throw new RuntimeException(
                        "Invalid coupon code"
                );
            }

            // Coupon applies to ticket + food subtotal
            var validation =
                    couponService.validateCoupon(
                            createCouponRequest(
                                    couponCode,
                                    combinedSubtotal,
                                    event.getId()
                            )
                    );

            if (!validation.isValid()) {

                throw new RuntimeException(
                        validation.getMessage()
                );
            }

            discountAmount =
                    validation.getDiscountAmount();
        }

        // =========================================================
        // FINAL TOTAL
        // =========================================================

        double totalAmount =
                combinedSubtotal -
                        discountAmount;

        // =========================================================
        // CREATE BOOKING
        // =========================================================

        Booking booking =
                new Booking();

        booking.setBookingReference(
                "TF-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase()
        );

        booking.setCustomer(customer);

        booking.setEvent(event);

        booking.setStatus(
                "PAYMENT_PENDING"
        );

        // Ticket subtotal
        booking.setSubtotalAmount(
                round(ticketSubtotalAmount)
        );

        // Food subtotal
        booking.setFoodSubtotalAmount(
                round(foodSubtotalAmount)
        );

        booking.setCouponCode(
                couponCode
        );

        booking.setDiscountAmount(
                round(discountAmount)
        );

        // Ticket + food - discount
        booking.setTotalAmount(
                round(totalAmount)
        );

        booking.setBookingTime(
                LocalDateTime.now()
        );

        Booking savedBooking =
                bookingRepository.save(
                        booking
                );

        // =========================================================
        // BOOKING SEATS
        // =========================================================

        // Seats remain HELD until successful payment.

        for (EventSeat eventSeat :
                selectedSeats) {

            BookingSeat bookingSeat =
                    new BookingSeat();

            bookingSeat.setBooking(
                    savedBooking
            );

            bookingSeat.setEventSeat(
                    eventSeat
            );

            bookingSeat.setPrice(
                    eventSeat.getPrice()
            );

            bookingSeatRepository.save(
                    bookingSeat
            );
        }

        // =========================================================
        // BOOKING FOOD ITEMS
        // =========================================================

        // Food stock is NOT reduced here.
        // Stock is reduced only after successful payment.

        if (requestedFoodItems != null) {

            for (BookingFoodItemRequest foodRequest :
                    requestedFoodItems) {

                if (foodRequest == null) {
                    continue;
                }

                StallItem stallItem =
                        stallItemRepository.findById(
                                foodRequest.getStallItemId()
                        ).orElseThrow(() ->
                                new RuntimeException(
                                        "Food item not found"
                                )
                        );

                double unitPrice =
                        stallItem.getPrice();

                double itemTotal =
                        unitPrice *
                                foodRequest.getQuantity();

                BookingFoodItem bookingFoodItem =
                        new BookingFoodItem();

                bookingFoodItem.setBooking(
                        savedBooking
                );

                bookingFoodItem.setStallItem(
                        stallItem
                );

                bookingFoodItem.setQuantity(
                        foodRequest.getQuantity()
                );

                bookingFoodItem.setUnitPrice(
                        round(unitPrice)
                );

                bookingFoodItem.setTotalPrice(
                        round(itemTotal)
                );

                bookingFoodItemRepository.save(
                        bookingFoodItem
                );
            }
        }

        return savedBooking;
    }

    // =========================================================
    // GET BOOKING BY ID
    // =========================================================

    public Booking getBookingById(Long id) {

        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found"
                        )
                );
    }

    // =========================================================
    // CREATE COUPON REQUEST
    // =========================================================

    private com.ticketflow.backend.dto.CouponValidateRequest
    createCouponRequest(
            String code,
            double subtotal,
            Long eventId) {

        com.ticketflow.backend.dto.CouponValidateRequest
                request =
                new com.ticketflow.backend.dto.CouponValidateRequest();

        request.setCode(code);
        request.setSubtotal(subtotal);
        request.setEventId(eventId);

        return request;
    }

    // =========================================================
    // ROUND
    // =========================================================

    private double round(double value) {

        return Math.round(value * 100.0)
                / 100.0;
    }

    // =========================================================
    // GET ORGANIZER BOOKINGS
    // =========================================================

    public List<Map<String, Object>> getOrganizerBookings(
            Long organizerId) {

        // ---------------------------------------------------------
        // VALIDATE ORGANIZER
        // ---------------------------------------------------------

        User organizer =
                userRepository.findById(organizerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Organizer not found"
                                )
                        );

        if (!"ORGANIZER".equals(
                organizer.getRole())) {

            throw new RuntimeException(
                    "User is not an organizer"
            );
        }

        // ---------------------------------------------------------
        // GET BOOKINGS FOR ORGANIZER EVENTS
        // ---------------------------------------------------------

        List<Booking> bookings =
                bookingRepository.findByEventOrganizerId(
                        organizerId
                );

        List<Map<String, Object>> response =
                new ArrayList<>();

        // ---------------------------------------------------------
        // CONVERT BOOKINGS TO SAFE RESPONSE
        // ---------------------------------------------------------

        for (Booking booking : bookings) {

            Map<String, Object> data =
                    new HashMap<>();

            // -----------------------------------------------------
            // BOOKING DETAILS
            // -----------------------------------------------------

            data.put(
                    "bookingId",
                    booking.getId()
            );

            data.put(
                    "bookingReference",
                    booking.getBookingReference()
            );

            data.put(
                    "bookingStatus",
                    booking.getStatus()
            );

            data.put(
                    "bookingTime",
                    booking.getBookingTime()
            );

            data.put(
                    "ticketSubtotal",
                    booking.getSubtotalAmount()
            );

            data.put(
                    "foodSubtotal",
                    booking.getFoodSubtotalAmount()
            );

            data.put(
                    "discountAmount",
                    booking.getDiscountAmount()
            );

            data.put(
                    "couponCode",
                    booking.getCouponCode()
            );

            data.put(
                    "totalAmount",
                    booking.getTotalAmount()
            );

            // -----------------------------------------------------
            // CUSTOMER
            // -----------------------------------------------------

            User customer =
                    booking.getCustomer();

            if (customer != null) {

                data.put(
                        "customerId",
                        customer.getId()
                );

                String firstName =
                        customer.getFirstName() == null
                                ? ""
                                : customer.getFirstName();

                String lastName =
                        customer.getLastName() == null
                                ? ""
                                : customer.getLastName();

                data.put(
                        "customerName",
                        (firstName + " " + lastName).trim()
                );

                data.put(
                        "customerEmail",
                        customer.getEmail()
                );

                data.put(
                        "customerPhone",
                        customer.getPhone()
                );
            }

            // -----------------------------------------------------
            // EVENT
            // -----------------------------------------------------

            Event event =
                    booking.getEvent();

            if (event != null) {

                data.put(
                        "eventId",
                        event.getId()
                );

                data.put(
                        "eventTitle",
                        event.getTitle()
                );

                data.put(
                        "eventDate",
                        event.getEventDate()
                );

                data.put(
                        "startTime",
                        event.getStartTime()
                );

                data.put(
                        "endTime",
                        event.getEndTime()
                );
            }

            // -----------------------------------------------------
            // SEATS
            // -----------------------------------------------------

            List<BookingSeat> bookingSeats =
                    bookingSeatRepository.findByBooking(
                            booking
                    );

            List<Map<String, Object>> seats =
                    new ArrayList<>();

            for (BookingSeat bookingSeat :
                    bookingSeats) {

                EventSeat eventSeat =
                        bookingSeat.getEventSeat();

                if (eventSeat == null ||
                        eventSeat.getSeat() == null) {

                    continue;
                }

                Map<String, Object> seat =
                        new HashMap<>();

                seat.put(
                        "eventSeatId",
                        eventSeat.getId()
                );

                seat.put(
                        "seatRow",
                        eventSeat.getSeat()
                                .getSeatRow()
                );

                seat.put(
                        "seatNumber",
                        eventSeat.getSeat()
                                .getSeatNumber()
                );

                seat.put(
                        "seatType",
                        eventSeat.getSeat()
                                .getSeatType()
                );

                seat.put(
                        "price",
                        bookingSeat.getPrice()
                );

                seats.add(seat);
            }

            data.put(
                    "seats",
                    seats
            );

            // -----------------------------------------------------
            // FOOD
            // -----------------------------------------------------

            List<BookingFoodItem> bookingFoodItems =
                    bookingFoodItemRepository.findByBooking(
                            booking
                    );

            List<Map<String, Object>> foodItems =
                    new ArrayList<>();

            for (BookingFoodItem bookingFoodItem :
                    bookingFoodItems) {

                if (bookingFoodItem == null ||
                        bookingFoodItem.getStallItem() == null) {

                    continue;
                }

                StallItem stallItem =
                        bookingFoodItem.getStallItem();

                Map<String, Object> food =
                        new HashMap<>();

                food.put(
                        "stallItemId",
                        stallItem.getId()
                );

                food.put(
                        "itemName",
                        stallItem.getItemName()
                );

                food.put(
                        "category",
                        stallItem.getCategory()
                );

                food.put(
                        "quantity",
                        bookingFoodItem.getQuantity()
                );

                food.put(
                        "unitPrice",
                        bookingFoodItem.getUnitPrice()
                );

                food.put(
                        "totalPrice",
                        bookingFoodItem.getTotalPrice()
                );

                if (stallItem.getStall() != null) {

                    food.put(
                            "stallId",
                            stallItem.getStall().getId()
                    );

                    food.put(
                            "stallName",
                            stallItem.getStall().getStallName()
                    );
                }

                foodItems.add(food);
            }

            data.put(
                    "foodItems",
                    foodItems
            );

            // -----------------------------------------------------
            // TICKET
            // -----------------------------------------------------

            ticketRepository
                    .findByBooking(booking)
                    .ifPresent(ticket -> {

                        data.put(
                                "ticketId",
                                ticket.getId()
                        );

                        data.put(
                                "ticketNumber",
                                ticket.getTicketNumber()
                        );

                        data.put(
                                "ticketStatus",
                                ticket.getStatus()
                        );
                    });

            // -----------------------------------------------------
            // ADD BOOKING
            // -----------------------------------------------------

            response.add(data);
        }

        return response;
    }
    public Map<String, Object> getBookingStatistics() {
    List<Booking> bookings = bookingRepository.findAll();

    long totalBookings = bookings.size();

    double totalRevenue = bookings.stream()
        .filter(booking ->
            "CONFIRMED".equalsIgnoreCase(booking.getStatus())
            || "COMPLETED".equalsIgnoreCase(booking.getStatus()))
        .mapToDouble(booking -> booking.getTotalAmount())
        .sum();

    Map<String, Object> statistics = new HashMap<>();
    statistics.put("totalBookings", totalBookings);
    statistics.put(
        "totalRevenue",
        Math.round(totalRevenue * 100.0) / 100.0
    );

    return statistics;
}

    // =========================================================
    // CANCELLATION & REFUNDS
    // =========================================================

    @Transactional
    public Booking cancelBooking(Long bookingId, String reason, User requestingUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        boolean isOwner = booking.getCustomer() != null && booking.getCustomer().getId().equals(requestingUser.getId());
        boolean isAdmin = "ADMIN".equals(requestingUser.getRole());

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You are not authorized to cancel this booking");
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getStatus()) || "REFUNDED".equalsIgnoreCase(booking.getStatus())) {
            throw new RuntimeException("Booking is already cancelled or refunded");
        }

        booking.setStatus("CANCELLED");
        booking.setCancellationReason(reason != null && !reason.trim().isEmpty() ? reason : "Requested by customer");
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        // 1. Release reserved seats back to AVAILABLE
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking(booking);
        for (BookingSeat bs : bookingSeats) {
            EventSeat es = bs.getEventSeat();
            if (es != null) {
                es.setStatus("AVAILABLE");
                es.setHeldBy(null);
                es.setHoldExpiresAt(null);
                eventSeatRepository.save(es);
            }
        }

        // 2. Invalidate ticket
        ticketRepository.findByBooking(booking).ifPresent(ticket -> {
            ticket.setStatus("CANCELLED");
            ticketRepository.save(ticket);
        });

        // 3. Create Refund record
        Refund refund = new Refund(booking, booking.getTotalAmount(), booking.getCancellationReason());
        refundRepository.save(refund);

        // 4. Activity Log
        activityLogService.logActivity(requestingUser, "BOOKING_CANCELLED",
                "Booking " + booking.getBookingReference() + " cancelled. Seats released, refund requested for ₹" + booking.getTotalAmount());

        // 5. Notify customer
        if (booking.getCustomer() != null) {
            notificationService.createNotification(
                    booking.getCustomer(),
                    "Booking Cancelled",
                    "Your booking " + booking.getBookingReference() + " for '" + (booking.getEvent() != null ? booking.getEvent().getTitle() : "Event") +
                            "' has been cancelled. A refund request of ₹" + booking.getTotalAmount() + " is submitted.",
                    "CANCELLATION"
            );
        }

        // 6. Notify organizer
        if (booking.getEvent() != null && booking.getEvent().getOrganizer() != null) {
            notificationService.createNotification(
                    booking.getEvent().getOrganizer(),
                    "Booking Cancelled",
                    "A booking for '" + booking.getEvent().getTitle() +
                            "' (" + bookingSeats.size() + " seats) was cancelled. Seats have been returned to available pool.",
                    "CANCELLATION"
            );
        }

        return booking;
    }

    public List<Refund> getAllRefunds() {
        return refundRepository.findAllByOrderByRequestedAtDesc();
    }

    @Transactional
    public Refund processRefund(Long refundId, User adminUser) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Refund request not found"));

        if ("PROCESSED".equalsIgnoreCase(refund.getStatus())) {
            throw new RuntimeException("Refund has already been processed");
        }

        refund.setStatus("PROCESSED");
        refund.setProcessedAt(LocalDateTime.now());
        refund.setTransactionReference("REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        Refund saved = refundRepository.save(refund);

        Booking booking = refund.getBooking();
        if (booking != null) {
            booking.setStatus("REFUNDED");
            bookingRepository.save(booking);

            if (booking.getCustomer() != null) {
                notificationService.createNotification(
                        booking.getCustomer(),
                        "Refund Processed Successfully",
                        "Your refund of ₹" + refund.getAmount() + " for booking " + booking.getBookingReference() +
                                " has been processed. Transaction Reference: " + refund.getTransactionReference(),
                        "REFUND"
                );
            }
        }

        activityLogService.logActivity(adminUser, "REFUND_PROCESSED",
                "Refund #" + refund.getId() + " of ₹" + refund.getAmount() + " processed by " + adminUser.getEmail());

        return saved;
    }
}