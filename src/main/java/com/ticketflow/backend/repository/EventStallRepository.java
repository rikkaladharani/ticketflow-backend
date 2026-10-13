package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.EventStall;
import com.ticketflow.backend.entity.Stall;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventStallRepository extends JpaRepository<EventStall, Long> {

    List<EventStall> findByEvent(Event event);

    List<EventStall> findByStall(Stall stall);

    Optional<EventStall> findByEventAndStall(Event event, Stall stall);

    List<EventStall> findByStatus(String status);
}