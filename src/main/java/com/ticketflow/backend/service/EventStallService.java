package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.EventStallRequest;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.EventStall;
import com.ticketflow.backend.entity.Stall;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.EventRepository;
import com.ticketflow.backend.repository.EventStallRepository;
import com.ticketflow.backend.repository.StallRepository;
import com.ticketflow.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventStallService {

    private final EventStallRepository eventStallRepository;
    private final EventRepository eventRepository;
    private final StallRepository stallRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public EventStallService(
            EventStallRepository eventStallRepository,
            EventRepository eventRepository,
            StallRepository stallRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            ActivityLogService activityLogService) {

        this.eventStallRepository = eventStallRepository;
        this.eventRepository = eventRepository;
        this.stallRepository = stallRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public EventStall applyForEvent(EventStallRequest request) {

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Stall stall = stallRepository.findById(request.getStallId())
                .orElseThrow(() -> new RuntimeException("Stall not found"));

        if (!"APPROVED".equals(stall.getStatus())
                && !"ACTIVE".equals(stall.getStatus())) {

            throw new RuntimeException(
                    "Stall must be approved before applying to an event"
            );
        }

        if (eventStallRepository
                .findByEventAndStall(event, stall)
                .isPresent()) {

            throw new RuntimeException(
                    "Stall has already applied to this event"
            );
        }

        EventStall eventStall = new EventStall();

        eventStall.setEvent(event);
        eventStall.setStall(stall);
        eventStall.setStatus("PENDING");

        EventStall saved = eventStallRepository.save(eventStall);

        if (event.getOrganizer() != null) {
            notificationService.createNotification(
                    event.getOrganizer().getId(),
                    "STALL_APPLICATION",
                    "New Stall Application: " + stall.getStallName(),
                    "Stall '" + stall.getStallName() + "' has applied to participate in your event '" + event.getTitle() + "'. Review their details and menu."
            );
        }
        if (stall.getOwner() != null) {
            activityLogService.log(stall.getOwner().getId(), "STALL_APPLIED", "Applied stall '" + stall.getStallName() + "' to event '" + event.getTitle() + "'");
        }

        return saved;
    }

    public List<EventStall> getAllEventStalls() {
        return eventStallRepository.findAll();
    }

    public List<EventStall> getEventStallsByEvent(Long eventId) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return eventStallRepository.findByEvent(event);
    }

    public List<EventStall> getEventStallsByStall(Long stallId) {

        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found"));

        return eventStallRepository.findByStall(stall);
    }

    public EventStall approveEventStall(
            Long eventStallId,
            Long organizerId) {

        EventStall eventStall = eventStallRepository.findById(eventStallId)
                .orElseThrow(() -> new RuntimeException("Event stall application not found"));

        Event event = eventStall.getEvent();

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (!"ORGANIZER".equals(organizer.getRole())) {
            throw new RuntimeException("User is not an organizer");
        }

        if (event.getOrganizer() == null
                || !event.getOrganizer().getId().equals(organizerId)) {

            throw new RuntimeException(
                    "Organizer does not own this event"
            );
        }

        if (!"PENDING".equals(eventStall.getStatus())) {
            throw new RuntimeException(
                    "This stall application is not pending"
            );
        }

        eventStall.setStatus("APPROVED");
        EventStall saved = eventStallRepository.save(eventStall);

        if (eventStall.getStall() != null && eventStall.getStall().getOwner() != null) {
            notificationService.createNotification(
                    eventStall.getStall().getOwner().getId(),
                    "STALL_APPROVED",
                    "Stall Approved for " + event.getTitle(),
                    "Congratulations! Your stall '" + eventStall.getStall().getStallName() + "' was APPROVED for event '" + event.getTitle() + "'. Attendees can now pre-order from your menu!"
            );
        }
        activityLogService.log(organizerId, "STALL_APPROVED", "Organizer approved stall '" + eventStall.getStall().getStallName() + "' for event '" + event.getTitle() + "'");

        return saved;
    }

    public EventStall rejectEventStall(
            Long eventStallId,
            Long organizerId) {

        EventStall eventStall = eventStallRepository.findById(eventStallId)
                .orElseThrow(() -> new RuntimeException("Event stall application not found"));

        Event event = eventStall.getEvent();

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));

        if (!"ORGANIZER".equals(organizer.getRole())) {
            throw new RuntimeException("User is not an organizer");
        }

        if (event.getOrganizer() == null
                || !event.getOrganizer().getId().equals(organizerId)) {

            throw new RuntimeException(
                    "Organizer does not own this event"
            );
        }

        if (!"PENDING".equals(eventStall.getStatus())) {
            throw new RuntimeException(
                    "This stall application is not pending"
            );
        }

        eventStall.setStatus("REJECTED");
        EventStall saved = eventStallRepository.save(eventStall);

        if (eventStall.getStall() != null && eventStall.getStall().getOwner() != null) {
            notificationService.createNotification(
                    eventStall.getStall().getOwner().getId(),
                    "STALL_REJECTED",
                    "Stall Application Update: " + event.getTitle(),
                    "Your application for stall '" + eventStall.getStall().getStallName() + "' was not accepted for event '" + event.getTitle() + "'."
            );
        }
        activityLogService.log(organizerId, "STALL_REJECTED", "Organizer rejected stall '" + eventStall.getStall().getStallName() + "' for event '" + event.getTitle() + "'");

        return saved;
    }
}