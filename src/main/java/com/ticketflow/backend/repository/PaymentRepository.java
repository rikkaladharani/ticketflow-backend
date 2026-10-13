package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.Payment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBooking(Booking booking);

    Optional<Payment> findByTransactionReference(
            String transactionReference);
}