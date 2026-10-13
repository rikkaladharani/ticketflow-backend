package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.CouponValidateRequest;
import com.ticketflow.backend.dto.CouponValidateResponse;
import com.ticketflow.backend.entity.Coupon;
import com.ticketflow.backend.service.CouponService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class CouponController {

    private final CouponService couponService;

    public CouponController(
            CouponService couponService) {

        this.couponService = couponService;
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponValidateResponse>
    validateCoupon(
            @RequestBody CouponValidateRequest request) {

        return ResponseEntity.ok(
                couponService.validateCoupon(request)
        );
    }

    @GetMapping("/organizer/{organizerId}")
    public ResponseEntity<List<Coupon>> getOrganizerCoupons(
            @PathVariable Long organizerId) {

        return ResponseEntity.ok(
                couponService.getCouponsByOrganizer(organizerId)
        );
    }

    @PostMapping("/organizer/{organizerId}")
    public ResponseEntity<?> createCoupon(
            @PathVariable Long organizerId,
            @RequestBody Coupon coupon) {

        try {
            Coupon created = couponService.createCoupon(coupon, organizerId);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleCoupon(
            @PathVariable Long id,
            @RequestParam(required = false) Long organizerId) {

        try {
            Coupon updated = couponService.toggleCouponStatus(id, organizerId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(
            @PathVariable Long id,
            @RequestParam(required = false) Long organizerId) {

        try {
            couponService.deleteCoupon(id, organizerId);
            return ResponseEntity.ok(Map.of("message", "Coupon deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}