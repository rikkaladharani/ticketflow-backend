package com.ticketflow.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bookingReference;

    @Column(nullable = false)
    private String status;

    /*
     * Original ticket/seat amount
     * before coupon discount.
     */
    @Column(nullable = false)
    private Double subtotalAmount;

    /*
     * Coupon code used for this booking.
     */
    private String couponCode;

    /*
     * Amount reduced by coupon.
     */
    @Column(nullable = false)
    private Double discountAmount;

    /*
     * Final amount actually paid.
     */
    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @ManyToOne
    @JoinColumn(
            name = "customer_id",
            nullable = false
    )
    private User customer;

    @ManyToOne
    @JoinColumn(
            name = "event_id",
            nullable = false
    )
    private Event event;

    @Column(nullable = true)
    private Double foodSubtotalAmount = 0.0;

    @Column(length = 1000)
    private String cancellationReason;

    private LocalDateTime cancelledAt;

    public Booking() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(
            String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(
            Double subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(
            Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setBookingTime(
            LocalDateTime bookingTime) {
        this.bookingTime = bookingTime;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Double getFoodSubtotalAmount() {
        return foodSubtotalAmount == null ? 0.0 : foodSubtotalAmount;
    }

    public void setFoodSubtotalAmount(Double foodSubtotalAmount) {
        this.foodSubtotalAmount = foodSubtotalAmount;
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