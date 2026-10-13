package com.ticketflow.backend.dto;

public class SeatGenerationRequest {

    private Long sectionId;
    private Double price;

    public Long getSectionId() {
        return sectionId;
    }

    public void setSectionId(Long sectionId) {
        this.sectionId = sectionId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}