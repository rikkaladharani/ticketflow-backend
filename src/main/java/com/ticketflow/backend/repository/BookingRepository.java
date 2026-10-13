package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByCustomer(User customer);

    List<Booking> findByCustomerOrderByBookingTimeDesc(User customer);

    List<Booking> findByEvent(Event event);

    List<Booking> findByEventOrganizerId(Long organizerId);
}
