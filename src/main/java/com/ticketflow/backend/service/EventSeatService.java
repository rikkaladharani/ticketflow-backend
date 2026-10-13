package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.EventSeatRequest;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.EventSeat;
import com.ticketflow.backend.entity.Seat;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.EventRepository;
import com.ticketflow.backend.repository.EventSeatRepository;
import com.ticketflow.backend.repository.SeatRepository;
import com.ticketflow.backend.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventSeatService {

    private final EventSeatRepository eventSeatRepository;
    private final EventRepository eventRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    public EventSeatService(
            EventSeatRepository eventSeatRepository,
            EventRepository eventRepository,
            SeatRepository seatRepository,
            UserRepository userRepository) {

        this.eventSeatRepository = eventSeatRepository;
        this.eventRepository = eventRepository;
        this.seatRepository = seatRepository;
        this.userRepository = userRepository;
    }

    public List<EventSeat> generateEventSeats(EventSeatRequest request) {

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        if (event.getVenue() == null) {
            throw new RuntimeException(
                    "Event does not have a venue assigned");
        }

        if (eventSeatRepository.existsByEvent(event)) {
            throw new RuntimeException(
                    "Event seats have already been generated");
        }

        List<Seat> allSeats = seatRepository.findAll();

        List<Seat> venueSeats = allSeats.stream()
                .filter(seat ->
                        seat.getSection()
                                .getVenue()
                                .getId()
                                .equals(event.getVenue().getId()))
                .toList();

        if (venueSeats.isEmpty()) {
            throw new RuntimeException(
                    "No seats found for this venue");
        }

        List<EventSeat> eventSeats = new ArrayList<>();

        for (Seat seat : venueSeats) {

            EventSeat eventSeat = new EventSeat();

            eventSeat.setEvent(event);
            eventSeat.setSeat(seat);
            eventSeat.setPrice(seat.getPrice());
            eventSeat.setStatus("AVAILABLE");

            eventSeats.add(eventSeat);
        }

        return eventSeatRepository.saveAll(eventSeats);
    }

    public List<EventSeat> getEventSeats(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        releaseExpiredHolds(event);

        return eventSeatRepository.findByEvent(event);
    }

    public List<EventSeat> getAvailableEventSeats(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));

        releaseExpiredHolds(event);

        return eventSeatRepository.findByEventAndStatus(
                event,
                "AVAILABLE"
        );
    }

    @Transactional
    public EventSeat holdSeat(
            Long eventSeatId,
            Long customerId) {

        EventSeat eventSeat = eventSeatRepository.findById(eventSeatId)
                .orElseThrow(() ->
                        new RuntimeException("Event seat not found"));

        User customer = userRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        if (!"CUSTOMER".equals(customer.getRole())) {
            throw new RuntimeException(
                    "Only customers can hold seats");
        }

        releaseExpiredHold(eventSeat);

        if (!"AVAILABLE".equals(eventSeat.getStatus())) {
            throw new RuntimeException(
                    "Seat is not available");
        }

        eventSeat.setStatus("HELD");
        eventSeat.setHeldBy(customer);
        eventSeat.setHoldExpiresAt(
                LocalDateTime.now().plusMinutes(10)
        );

        return eventSeatRepository.save(eventSeat);
    }

    @Transactional
    public EventSeat releaseSeat(
            Long eventSeatId,
            Long customerId) {

        EventSeat eventSeat = eventSeatRepository.findById(eventSeatId)
                .orElseThrow(() ->
                        new RuntimeException("Event seat not found"));

        releaseExpiredHold(eventSeat);

        if (!"HELD".equals(eventSeat.getStatus())) {
            throw new RuntimeException(
                    "Seat is not currently held");
        }

        if (eventSeat.getHeldBy() == null ||
                !eventSeat.getHeldBy().getId().equals(customerId)) {

            throw new RuntimeException(
                    "You do not own this seat hold");
        }

        eventSeat.setStatus("AVAILABLE");
        eventSeat.setHeldBy(null);
        eventSeat.setHoldExpiresAt(null);

        return eventSeatRepository.save(eventSeat);
    }

    private void releaseExpiredHold(EventSeat eventSeat) {

        if ("HELD".equals(eventSeat.getStatus())
                && eventSeat.getHoldExpiresAt() != null
                && eventSeat.getHoldExpiresAt()
                        .isBefore(LocalDateTime.now())) {

            eventSeat.setStatus("AVAILABLE");
            eventSeat.setHeldBy(null);
            eventSeat.setHoldExpiresAt(null);

            eventSeatRepository.save(eventSeat);
        }
    }

    private void releaseExpiredHolds(Event event) {

        List<EventSeat> eventSeats =
                eventSeatRepository.findByEvent(event);

        for (EventSeat eventSeat : eventSeats) {
            releaseExpiredHold(eventSeat);
        }
    }
}