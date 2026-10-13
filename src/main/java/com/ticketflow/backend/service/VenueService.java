package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.VenueRequest;
import com.ticketflow.backend.entity.Venue;
import com.ticketflow.backend.repository.VenueRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue createVenue(VenueRequest request) {

        Venue venue = new Venue();

        venue.setName(request.getName());
        venue.setAddress(request.getAddress());
        venue.setCity(request.getCity());
        venue.setCapacity(request.getCapacity());
        venue.setImageUrl(request.getImageUrl());
        venue.setDescription(request.getDescription());
        venue.setLayoutType(request.getLayoutType());

        return venueRepository.save(venue);
    }

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Venue getVenueById(Long id) {

        return venueRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Venue not found"));
    }

    public List<Venue> getVenuesByCity(String city) {
        return venueRepository.findByCity(city);
    }

    public List<Venue> getVenuesByLayoutType(String layoutType) {
        return venueRepository.findByLayoutType(layoutType);
    }
}