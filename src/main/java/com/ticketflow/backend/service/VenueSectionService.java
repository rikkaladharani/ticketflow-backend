package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.VenueSectionRequest;
import com.ticketflow.backend.entity.Venue;
import com.ticketflow.backend.entity.VenueSection;
import com.ticketflow.backend.repository.VenueRepository;
import com.ticketflow.backend.repository.VenueSectionRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueSectionService {

    private final VenueSectionRepository venueSectionRepository;
    private final VenueRepository venueRepository;

    public VenueSectionService(
            VenueSectionRepository venueSectionRepository,
            VenueRepository venueRepository) {

        this.venueSectionRepository = venueSectionRepository;
        this.venueRepository = venueRepository;
    }

    public VenueSection createSection(VenueSectionRequest request) {

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() ->
                        new RuntimeException("Venue not found"));

        VenueSection section = new VenueSection();

        section.setName(request.getName());
        section.setSectionType(request.getSectionType());
        section.setRowCount(request.getRowCount());
        section.setSeatsPerRow(request.getSeatsPerRow());
        section.setVenue(venue);

        return venueSectionRepository.save(section);
    }

    public List<VenueSection> getAllSections() {
        return venueSectionRepository.findAll();
    }

    public VenueSection getSectionById(Long id) {

        return venueSectionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Venue section not found"));
    }

    public List<VenueSection> getSectionsByVenue(Long venueId) {

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() ->
                        new RuntimeException("Venue not found"));

        return venueSectionRepository.findByVenue(venue);
    }

    public List<VenueSection> getSectionsByType(String sectionType) {
        return venueSectionRepository.findBySectionType(sectionType);
    }
}