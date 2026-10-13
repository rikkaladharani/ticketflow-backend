package com.ticketflow.backend.dto;

public class CancellationRequest {

    private String reason;

    public CancellationRequest() {
    }

    public CancellationRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
