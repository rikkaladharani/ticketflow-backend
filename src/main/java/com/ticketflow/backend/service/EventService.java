
package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.EventRequest;
import com.ticketflow.backend.dto.EventSeatRequest;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.entity.Venue;
import com.ticketflow.backend.repository.EventRepository;
import com.ticketflow.backend.repository.UserRepository;
import com.ticketflow.backend.repository.VenueRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final EventSeatService eventSeatService;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public EventService(
            EventRepository eventRepository,
            UserRepository userRepository,
            VenueRepository venueRepository,
            EventSeatService eventSeatService,
            NotificationService notificationService,
            ActivityLogService activityLogService) {

        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
        this.eventSeatService = eventSeatService;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    @Transactional
    public Event createEvent(EventRequest request) {

        User organizer = userRepository.findById(
                request.getOrganizerId()
        ).orElseThrow(() ->
                new RuntimeException("Organizer not found"));

        if (!"ORGANIZER".equals(organizer.getRole())) {
            throw new RuntimeException(
                    "Only an organizer can create an event");
        }

        Venue venue = null;

        if (request.getVenueId() != null) {

            venue = venueRepository.findById(
                    request.getVenueId()
            ).orElseThrow(() ->
                    new RuntimeException("Venue not found"));
        }

        Event event = new Event();

        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setCategory(request.getCategory());
        event.setImageUrl(request.getImageUrl());
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setHasStalls(request.getHasStalls() != null ? request.getHasStalls() : false);

        event.setStatus("PENDING");
        event.setOrganizer(organizer);
        event.setVenue(venue);

        // Save the event first so it receives an ID.
        Event savedEvent = eventRepository.save(event);

        // Automatically generate seats for the selected venue.
        if (savedEvent.getVenue() != null) {

            EventSeatRequest seatRequest = new EventSeatRequest();
            seatRequest.setEventId(savedEvent.getId());

            eventSeatService.generateEventSeats(seatRequest);
        }

        // Notify stall owners if stalls are requested for this event
        if (Boolean.TRUE.equals(savedEvent.getHasStalls())) {
            List<User> stallOwners = userRepository.findAll().stream()
                    .filter(u -> "STALL_OWNER".equalsIgnoreCase(u.getRole()))
                    .toList();
            for (User owner : stallOwners) {
                notificationService.createNotification(
                        owner.getId(),
                        "STALL_INVITATION",
                        "New Event Open for Stalls: " + savedEvent.getTitle(),
                        "Organizer posted a new event '" + savedEvent.getTitle() + "' and is accepting stall applications. Check available events to submit your stall and menu!"
                );
            }
            activityLogService.log(organizer.getId(), "EVENT_CREATED_WITH_STALLS", "Created event with stalls enabled: " + savedEvent.getTitle());
        } else {
            activityLogService.log(organizer.getId(), "EVENT_CREATED", "Created event: " + savedEvent.getTitle());
        }

        return savedEvent;
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event getEventById(Long id) {

        return eventRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Event not found"));
    }

    public List<Event> getEventsByOrganizer(Long organizerId) {

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() ->
                        new RuntimeException("Organizer not found"));

        return eventRepository.findByOrganizer(organizer);
    }

    public List<Event> getEventsByCategory(String category) {
        return eventRepository.findByCategory(category);
    }
    
@Transactional
public Event approveEvent(Long eventId) {

    Event event = eventRepository.findById(eventId)
            .orElseThrow(() ->
                    new RuntimeException("Event not found"));

    if (!"PENDING".equalsIgnoreCase(event.getStatus())) {
        throw new RuntimeException(
                "Only pending events can be approved");
    }

    event.setStatus("APPROVED");

    return eventRepository.save(event);
}

@Transactional
public Event rejectEvent(Long eventId) {

    Event event = eventRepository.findById(eventId)
            .orElseThrow(() ->
                    new RuntimeException("Event not found"));

    if (!"PENDING".equalsIgnoreCase(event.getStatus())) {
        throw new RuntimeException(
                "Only pending events can be rejected");
    }

    event.setStatus("REJECTED");

    return eventRepository.save(event);
}

}
