package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(User organizer);

    List<Event> findByCategory(String category);

    List<Event> findByStatus(String status);
}