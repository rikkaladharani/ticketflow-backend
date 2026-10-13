package com.ticketflow.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CustomerTicketResponse {

    private Long ticketId;
    private String ticketNumber;
    private String ticketStatus;
    private String qrCodeData;
    private String qrCodeImage;
    private LocalDateTime issuedAt;
    private Boolean checkedIn;
    private LocalDateTime checkedInAt;
    private String cancellationReason;
    private LocalDateTime cancelledAt;

    private Long bookingId;
    private String bookingReference;
    private String bookingStatus;

    private Double subtotalAmount;
    private Double foodSubtotalAmount;
    private Double discountAmount;
    private Double totalAmount;

    private String couponCode;

    private LocalDateTime bookingTime;

    private Long eventId;
    private String eventTitle;
    private String eventDescription;
    private String eventCategory;
    private String eventImageUrl;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String venueName;
    private String venueAddress;
    private String venueCity;

    private List<SeatInfo> seats;

    private List<FoodInfo> foodItems;

    public static class SeatInfo {

        private Long eventSeatId;
        private String seatRow;
        private Integer seatNumber;
        private String seatType;
        private Double price;

        public SeatInfo() {
        }

        public SeatInfo(
                Long eventSeatId,
                String seatRow,
                Integer seatNumber,
                String seatType,
                Double price) {

            this.eventSeatId = eventSeatId;
            this.seatRow = seatRow;
            this.seatNumber = seatNumber;
            this.seatType = seatType;
            this.price = price;
        }

        public Long getEventSeatId() {
            return eventSeatId;
        }

        public void setEventSeatId(Long eventSeatId) {
            this.eventSeatId = eventSeatId;
        }

        public String getSeatRow() {
            return seatRow;
        }

        public void setSeatRow(String seatRow) {
            this.seatRow = seatRow;
        }

        public Integer getSeatNumber() {
            return seatNumber;
        }

        public void setSeatNumber(Integer seatNumber) {
            this.seatNumber = seatNumber;
        }

        public String getSeatType() {
            return seatType;
        }

        public void setSeatType(String seatType) {
            this.seatType = seatType;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }

    public static class FoodInfo {

        private Long stallItemId;
        private String itemName;
        private String description;
        private String category;

        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;

        private Long stallId;
        private String stallName;

        public FoodInfo() {
        }

        public FoodInfo(
                Long stallItemId,
                String itemName,
                String description,
                String category,
                Integer quantity,
                Double unitPrice,
                Double totalPrice,
                Long stallId,
                String stallName) {

            this.stallItemId = stallItemId;
            this.itemName = itemName;
            this.description = description;
            this.category = category;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
            this.stallId = stallId;
            this.stallName = stallName;
        }

        public Long getStallItemId() {
            return stallItemId;
        }

        public void setStallItemId(Long stallItemId) {
            this.stallItemId = stallItemId;
        }

        public String getItemName() {
            return itemName;
        }

        public void setItemName(String itemName) {
            this.itemName = itemName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(Double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public Long getStallId() {
            return stallId;
        }

        public void setStallId(Long stallId) {
            this.stallId = stallId;
        }

        public String getStallName() {
            return stallName;
        }

        public void setStallName(String stallName) {
            this.stallName = stallName;
        }
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public String getQrCodeImage() {
        return qrCodeImage;
    }

    public void setQrCodeImage(String qrCodeImage) {
        this.qrCodeImage = qrCodeImage;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Double getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(Double subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public Double getFoodSubtotalAmount() {
        return foodSubtotalAmount;
    }

    public void setFoodSubtotalAmount(Double foodSubtotalAmount) {
        this.foodSubtotalAmount = foodSubtotalAmount;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventImageUrl() {
        return eventImageUrl;
    }

    public void setEventImageUrl(String eventImageUrl) {
        this.eventImageUrl = eventImageUrl;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress) {
        this.venueAddress = venueAddress;
    }

    public String getVenueCity() {
        return venueCity;
    }

    public void setVenueCity(String venueCity) {
        this.venueCity = venueCity;
    }

    public List<SeatInfo> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatInfo> seats) {
        this.seats = seats;
    }

    public List<FoodInfo> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<FoodInfo> foodItems) {
        this.foodItems = foodItems;
    }

    public Boolean getCheckedIn() {
        return checkedIn != null ? checkedIn : false;
    }

    public void setCheckedIn(Boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}