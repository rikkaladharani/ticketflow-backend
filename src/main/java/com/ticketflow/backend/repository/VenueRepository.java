package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByCity(String city);

    List<Venue> findByLayoutType(String layoutType);
}