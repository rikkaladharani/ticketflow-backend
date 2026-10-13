package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.PaymentRequest;
import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.BookingFoodItem;
import com.ticketflow.backend.entity.BookingSeat;
import com.ticketflow.backend.entity.Coupon;
import com.ticketflow.backend.entity.EventSeat;
import com.ticketflow.backend.entity.Payment;
import com.ticketflow.backend.entity.StallItem;
import com.ticketflow.backend.repository.BookingFoodItemRepository;
import com.ticketflow.backend.repository.BookingRepository;
import com.ticketflow.backend.repository.BookingSeatRepository;
import com.ticketflow.backend.repository.EventSeatRepository;
import com.ticketflow.backend.repository.PaymentRepository;
import com.ticketflow.backend.repository.StallItemRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingFoodItemRepository bookingFoodItemRepository;
    private final EventSeatRepository eventSeatRepository;
    private final StallItemRepository stallItemRepository;
    private final CouponService couponService;

    public PaymentService(
            PaymentRepository paymentRepository,
            BookingRepository bookingRepository,
            BookingSeatRepository bookingSeatRepository,
            BookingFoodItemRepository bookingFoodItemRepository,
            EventSeatRepository eventSeatRepository,
            StallItemRepository stallItemRepository,
            CouponService couponService) {

        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.bookingFoodItemRepository = bookingFoodItemRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.stallItemRepository = stallItemRepository;
        this.couponService = couponService;
    }

    @Transactional
    public Payment processPayment(PaymentRequest request) {

        /*
         * -----------------------------------------
         * FIND BOOKING
         * -----------------------------------------
         */

        Booking booking =
                bookingRepository.findById(
                        request.getBookingId()
                ).orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found"));

        /*
         * -----------------------------------------
         * BOOKING STATUS
         * -----------------------------------------
         */

        if (!"PAYMENT_PENDING".equals(
                booking.getStatus())) {

            throw new RuntimeException(
                    "Booking is not awaiting payment");
        }

        /*
         * -----------------------------------------
         * PREVENT DUPLICATE PAYMENT
         * -----------------------------------------
         */

        if (paymentRepository.findByBooking(
                booking).isPresent()) {

            throw new RuntimeException(
                    "Payment already exists for this booking");
        }

        /*
         * -----------------------------------------
         * PAYMENT METHOD
         * -----------------------------------------
         */

        String method =
                request.getPaymentMethod();

        if (method == null ||
                (!method.equals("UPI")
                        && !method.equals("CARD")
                        && !method.equals("NET_BANKING"))) {

            throw new RuntimeException(
                    "Invalid payment method");
        }

        /*
         * -----------------------------------------
         * GET BOOKING SEATS
         * -----------------------------------------
         */

        List<BookingSeat> bookingSeats =
                bookingSeatRepository.findByBooking(
                        booking);

        /*
         * -----------------------------------------
         * VALIDATE ALL SEATS FIRST
         * -----------------------------------------
         *
         * We validate every seat before changing
         * any seat or food stock.
         */

        for (BookingSeat bookingSeat :
                bookingSeats) {

            EventSeat eventSeat =
                    bookingSeat.getEventSeat();

            if (!"HELD".equals(
                    eventSeat.getStatus())) {

                throw new RuntimeException(
                        "One or more seats are no longer held");
            }

            if (eventSeat.getHeldBy() == null ||
                    !eventSeat.getHeldBy()
                            .getId()
                            .equals(
                                    booking.getCustomer()
                                            .getId())) {

                throw new RuntimeException(
                        "One or more seats are not held by this customer");
            }

            if (eventSeat.getHoldExpiresAt() != null &&
                    eventSeat.getHoldExpiresAt()
                            .isBefore(
                                    LocalDateTime.now())) {

                throw new RuntimeException(
                        "One or more seat holds have expired");
            }
        }

        /*
         * -----------------------------------------
         * GET BOOKING FOOD
         * -----------------------------------------
         */

        List<BookingFoodItem> bookingFoodItems =
                bookingFoodItemRepository.findByBooking(
                        booking);

        /*
         * -----------------------------------------
         * VALIDATE FOOD STOCK FIRST
         * -----------------------------------------
         *
         * We do NOT reduce stock yet.
         */

        for (BookingFoodItem bookingFoodItem :
                bookingFoodItems) {

            StallItem stallItem =
                    bookingFoodItem.getStallItem();

            if (!Boolean.TRUE.equals(
                    stallItem.getAvailable())) {

                throw new RuntimeException(
                        "Food item is no longer available: " +
                                stallItem.getItemName());
            }

            if (stallItem.getAvailableQuantity() <
                    bookingFoodItem.getQuantity()) {

                throw new RuntimeException(
                        "Not enough stock for: " +
                                stallItem.getItemName());
            }
        }

        /*
         * -----------------------------------------
         * CREATE PAYMENT
         * -----------------------------------------
         */

        Payment payment =
                new Payment();

        payment.setTransactionReference(
                "TXN-" +
                        UUID.randomUUID()
                                .toString()
                                .substring(0, 8)
                                .toUpperCase()
        );

        payment.setAmount(
                booking.getTotalAmount());

        payment.setPaymentMethod(
                method);

        payment.setStatus(
                "SUCCESS");

        payment.setPaymentTime(
                LocalDateTime.now());

        payment.setBooking(
                booking);

        Payment savedPayment =
                paymentRepository.save(
                        payment);

        /*
         * -----------------------------------------
         * CONFIRM BOOKING
         * -----------------------------------------
         */

        booking.setStatus(
                "CONFIRMED");

        bookingRepository.save(
                booking);

        /*
         * -----------------------------------------
         * COUPON USAGE
         * -----------------------------------------
         */

        if (booking.getCouponCode() != null &&
                !booking.getCouponCode()
                        .trim()
                        .isEmpty()) {

            Coupon coupon =
                    couponService.getValidCoupon(
                            booking.getCouponCode());

            if (coupon != null) {

                couponService.incrementUsage(
                        coupon);
            }
        }

        /*
         * -----------------------------------------
         * BOOK SEATS
         * -----------------------------------------
         */

        for (BookingSeat bookingSeat :
                bookingSeats) {

            EventSeat eventSeat =
                    bookingSeat.getEventSeat();

            eventSeat.setStatus(
                    "BOOKED");

            eventSeat.setHeldBy(
                    null);

            eventSeat.setHoldExpiresAt(
                    null);

            eventSeatRepository.save(
                    eventSeat);
        }

        /*
         * -----------------------------------------
         * REDUCE FOOD STOCK
         * -----------------------------------------
         *
         * This happens ONLY after successful
         * payment and after all validations.
         */

        for (BookingFoodItem bookingFoodItem :
                bookingFoodItems) {

            StallItem stallItem =
                    bookingFoodItem.getStallItem();

            int remainingQuantity =
                    stallItem.getAvailableQuantity()
                            - bookingFoodItem.getQuantity();

            stallItem.setAvailableQuantity(
                    remainingQuantity);

            /*
             * If stock becomes zero, mark the
             * item unavailable.
             */

            if (remainingQuantity <= 0) {

                stallItem.setAvailableQuantity(0);

                stallItem.setAvailable(false);
            }

            stallItemRepository.save(
                    stallItem);
        }

        /*
         * -----------------------------------------
         * RETURN PAYMENT
         * -----------------------------------------
         */

        return savedPayment;
    }
}