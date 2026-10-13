package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.Ticket;
import com.ticketflow.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByBooking(Booking booking);

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByBookingCustomer(User customer);
}