package com.ticketflow.backend.dto;

public class BookingFoodItemRequest {

    private Long stallItemId;
    private Integer quantity;

    public BookingFoodItemRequest() {
    }

    public Long getStallItemId() {
        return stallItemId;
    }

    public void setStallItemId(Long stallItemId) {
        this.stallItemId = stallItemId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}