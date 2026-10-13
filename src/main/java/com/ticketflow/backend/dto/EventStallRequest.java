package com.ticketflow.backend.dto;

public class EventStallRequest {

    private Long eventId;
    private Long stallId;

    public EventStallRequest() {
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getStallId() {
        return stallId;
    }

    public void setStallId(Long stallId) {
        this.stallId = stallId;
    }
}