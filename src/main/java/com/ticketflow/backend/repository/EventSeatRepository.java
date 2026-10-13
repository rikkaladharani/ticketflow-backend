package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.EventSeat;
import com.ticketflow.backend.entity.Seat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventSeatRepository
        extends JpaRepository<EventSeat, Long> {

    List<EventSeat> findByEvent(Event event);

    List<EventSeat> findByEventAndStatus(Event event, String status);

    Optional<EventSeat> findByEventAndSeat(Event event, Seat seat);

    boolean existsByEvent(Event event);
}