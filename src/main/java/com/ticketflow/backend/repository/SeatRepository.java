package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Seat;
import com.ticketflow.backend.entity.VenueSection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findBySection(VenueSection section);

    List<Seat> findBySectionAndStatus(
            VenueSection section,
            String status
    );

    long countBySection(VenueSection section);
}