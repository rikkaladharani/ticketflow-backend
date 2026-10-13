package com.ticketflow.backend.dto;

public class CheckInRequest {

    private String code; // ticketNumber or qrCodeData

    public CheckInRequest() {
    }

    public CheckInRequest(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
