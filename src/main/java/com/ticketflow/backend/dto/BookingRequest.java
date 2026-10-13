package com.ticketflow.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class BookingRequest {

    private Long customerId;
    private Long eventId;
    private List<Long> eventSeatIds;
    private String couponCode;

    private List<BookingFoodItemRequest> foodItems = new ArrayList<>();

    public BookingRequest() {
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public List<Long> getEventSeatIds() {
        return eventSeatIds;
    }

    public void setEventSeatIds(List<Long> eventSeatIds) {
        this.eventSeatIds = eventSeatIds;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public List<BookingFoodItemRequest> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<BookingFoodItemRequest> foodItems) {
        this.foodItems = foodItems;
    }
}