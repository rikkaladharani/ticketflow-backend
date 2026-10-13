package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Venue;
import com.ticketflow.backend.entity.VenueSection;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueSectionRepository
        extends JpaRepository<VenueSection, Long> {

    List<VenueSection> findByVenue(Venue venue);

    List<VenueSection> findBySectionType(String sectionType);
}