package com.ticketflow.backend.dto;

public class CouponValidateResponse {

    private boolean valid;

    private String message;

    private String code;

    private String discountType;

    private Double discountAmount;

    private Double subtotal;

    private Double totalAmount;

    public CouponValidateResponse() {
    }

    public CouponValidateResponse(
            boolean valid,
            String message,
            String code,
            String discountType,
            Double discountAmount,
            Double subtotal,
            Double totalAmount) {

        this.valid = valid;
        this.message = message;
        this.code = code;
        this.discountType = discountType;
        this.discountAmount = discountAmount;
        this.subtotal = subtotal;
        this.totalAmount = totalAmount;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}