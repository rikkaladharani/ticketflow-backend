package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.CouponValidateRequest;
import com.ticketflow.backend.dto.CouponValidateResponse;
import com.ticketflow.backend.entity.Coupon;
import com.ticketflow.backend.repository.CouponRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public CouponValidateResponse validateCoupon(
            CouponValidateRequest request) {

        if (request.getCode() == null ||
                request.getCode().trim().isEmpty()) {

            return invalidResponse(
                    "Please enter a coupon code");
        }

        if (request.getSubtotal() == null ||
                request.getSubtotal() <= 0) {

            return invalidResponse(
                    "Invalid order amount");
        }

        String code =
                request.getCode()
                        .trim()
                        .toUpperCase();

        double subtotal = request.getSubtotal();

        Coupon coupon =
                couponRepository.findByCode(code)
                        .orElse(null);

        if (coupon == null) {
            return invalidResponse(
                    "Invalid coupon code");
        }

        if (!Boolean.TRUE.equals(coupon.getActive())) {
            return invalidResponse(
                    "This coupon is no longer active");
        }

        LocalDateTime now =
                LocalDateTime.now();

        if (coupon.getValidFrom() != null &&
                now.isBefore(coupon.getValidFrom())) {

            return invalidResponse(
                    "This coupon is not active yet");
        }

        if (coupon.getValidUntil() != null &&
                now.isAfter(coupon.getValidUntil())) {

            return invalidResponse(
                    "This coupon has expired");
        }

        if (coupon.getUsageLimit() != null &&
                coupon.getUsedCount() >=
                        coupon.getUsageLimit()) {

            return invalidResponse(
                    "This coupon has reached its usage limit");
        }

        // Check if coupon is tied to a specific event
        if (coupon.getEventId() != null) {
            if (request.getEventId() == null || !coupon.getEventId().equals(request.getEventId())) {
                String eventTitle = coupon.getEventTitle() != null && !coupon.getEventTitle().trim().isEmpty()
                        ? "'" + coupon.getEventTitle() + "'"
                        : "a specific event";
                return invalidResponse(
                        "This coupon is only valid for " + eventTitle);
            }
        }

        if (coupon.getMinOrderAmount() != null && subtotal < coupon.getMinOrderAmount()) {

            return invalidResponse(
                    "Minimum order amount is \u20B9" +
                    formatAmount(
                            coupon.getMinOrderAmount()));
        }

        double discountAmount;

        if ("PERCENTAGE".equalsIgnoreCase(
                coupon.getDiscountType())) {

            discountAmount =
                    subtotal *
                    coupon.getDiscountValue() /
                    100.0;

            if (coupon.getMaxDiscount() != null &&
                    discountAmount >
                            coupon.getMaxDiscount()) {

                discountAmount =
                        coupon.getMaxDiscount();
            }

        } else if ("FIXED".equalsIgnoreCase(
                coupon.getDiscountType())) {

            discountAmount =
                    coupon.getDiscountValue();

        } else {

            return invalidResponse(
                    "Invalid coupon configuration");
        }

        /*
         * Discount can never be greater
         * than the order amount.
         */
        discountAmount =
                Math.min(discountAmount, subtotal);

        discountAmount =
                Math.max(discountAmount, 0);

        double totalAmount =
                subtotal - discountAmount;

        return new CouponValidateResponse(
                true,
                "Coupon applied successfully! You saved \u20B9" + formatAmount(round(discountAmount)),
                coupon.getCode(),
                coupon.getDiscountType(),
                round(discountAmount),
                round(subtotal),
                round(totalAmount)
        );
    }

    public Coupon getValidCoupon(String code) {

        if (code == null ||
                code.trim().isEmpty()) {

            return null;
        }

        return couponRepository
                .findByCode(
                        code.trim().toUpperCase()
                )
                .orElse(null);
    }

    public void incrementUsage(Coupon coupon) {

        if (coupon == null) {
            return;
        }

        int current =
                coupon.getUsedCount() == null
                        ? 0
                        : coupon.getUsedCount();

        coupon.setUsedCount(current + 1);

        couponRepository.save(coupon);
    }

    public List<Coupon> getCouponsByOrganizer(Long organizerId) {
        if (organizerId == null) {
            return Collections.emptyList();
        }
        return couponRepository.findByOrganizerIdOrderByIdDesc(organizerId);
    }

    public Coupon createCoupon(Coupon coupon, Long organizerId) {
        if (coupon == null) {
            throw new RuntimeException("Coupon data is required");
        }
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new RuntimeException("Coupon code is required");
        }

        String formattedCode = coupon.getCode().trim().toUpperCase().replaceAll("\\s+", "");
        if (formattedCode.length() < 3) {
            throw new RuntimeException("Coupon code must be at least 3 characters");
        }

        if (couponRepository.existsByCode(formattedCode)) {
            throw new RuntimeException("Coupon code '" + formattedCode + "' already exists. Please choose a unique code.");
        }

        coupon.setCode(formattedCode);
        coupon.setOrganizerId(organizerId);

        if (coupon.getDiscountType() == null || (!coupon.getDiscountType().equalsIgnoreCase("PERCENTAGE") && !coupon.getDiscountType().equalsIgnoreCase("FIXED"))) {
            coupon.setDiscountType("PERCENTAGE");
        } else {
            coupon.setDiscountType(coupon.getDiscountType().toUpperCase());
        }

        if (coupon.getDiscountValue() == null || coupon.getDiscountValue() <= 0) {
            throw new RuntimeException("Discount amount must be greater than zero");
        }

        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType()) && coupon.getDiscountValue() > 100) {
            throw new RuntimeException("Percentage discount cannot exceed 100%");
        }

        if (coupon.getMinOrderAmount() == null) {
            coupon.setMinOrderAmount(0.0);
        }

        if (coupon.getUsedCount() == null) {
            coupon.setUsedCount(0);
        }

        if (coupon.getActive() == null) {
            coupon.setActive(true);
        }

        return couponRepository.save(coupon);
    }

    public Coupon toggleCouponStatus(Long id, Long organizerId) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        if (organizerId != null && coupon.getOrganizerId() != null && !coupon.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("You are not authorized to modify this coupon");
        }

        coupon.setActive(!Boolean.TRUE.equals(coupon.getActive()));
        return couponRepository.save(coupon);
    }

    public void deleteCoupon(Long id, Long organizerId) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found with id: " + id));

        if (organizerId != null && coupon.getOrganizerId() != null && !coupon.getOrganizerId().equals(organizerId)) {
            throw new RuntimeException("You are not authorized to delete this coupon");
        }

        couponRepository.delete(coupon);
    }

    private CouponValidateResponse invalidResponse(
            String message) {

        return new CouponValidateResponse(
                false,
                message,
                null,
                null,
                0.0,
                0.0,
                0.0
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String formatAmount(double value) {
        return String.format(
                "%.0f",
                value
        );
    }
}