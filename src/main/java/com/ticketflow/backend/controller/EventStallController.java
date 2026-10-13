package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.EventStallRequest;
import com.ticketflow.backend.entity.EventStall;
import com.ticketflow.backend.service.EventStallService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event-stalls")
@CrossOrigin
public class EventStallController {

    private final EventStallService eventStallService;

    public EventStallController(EventStallService eventStallService) {
        this.eventStallService = eventStallService;
    }

    @PostMapping
    public ResponseEntity<EventStall> applyForEvent(
            @RequestBody EventStallRequest request) {

        return ResponseEntity.ok(
                eventStallService.applyForEvent(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<EventStall>> getAllEventStalls() {

        return ResponseEntity.ok(
                eventStallService.getAllEventStalls()
        );
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<EventStall>> getEventStallsByEvent(
            @PathVariable Long eventId) {

        return ResponseEntity.ok(
                eventStallService.getEventStallsByEvent(eventId)
        );
    }

    @GetMapping("/stall/{stallId}")
    public ResponseEntity<List<EventStall>> getEventStallsByStall(
            @PathVariable Long stallId) {

        return ResponseEntity.ok(
                eventStallService.getEventStallsByStall(stallId)
        );
    }

    @PutMapping("/{eventStallId}/approve")
    public ResponseEntity<Map<String, Object>> approveEventStall(
            @PathVariable Long eventStallId,
            @RequestParam Long organizerId) {

        EventStall eventStall =
                eventStallService.approveEventStall(
                        eventStallId,
                        organizerId
                );

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Stall approved successfully");
        response.put("eventStallId", eventStall.getId());
        response.put("eventId", eventStall.getEvent().getId());
        response.put("stallId", eventStall.getStall().getId());
        response.put("status", eventStall.getStatus());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{eventStallId}/reject")
    public ResponseEntity<Map<String, Object>> rejectEventStall(
            @PathVariable Long eventStallId,
            @RequestParam Long organizerId) {

        EventStall eventStall =
                eventStallService.rejectEventStall(
                        eventStallId,
                        organizerId
                );

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Stall rejected successfully");
        response.put("eventStallId", eventStall.getId());
        response.put("eventId", eventStall.getEvent().getId());
        response.put("stallId", eventStall.getStall().getId());
        response.put("status", eventStall.getStatus());

        return ResponseEntity.ok(response);
    }
}