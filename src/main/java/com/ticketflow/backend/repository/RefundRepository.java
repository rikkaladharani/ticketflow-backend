package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {

    Optional<Refund> findByBooking(Booking booking);

    List<Refund> findAllByOrderByRequestedAtDesc();

    List<Refund> findByStatusOrderByRequestedAtDesc(String status);
}
