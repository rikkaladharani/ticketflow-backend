package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Coupon;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository
        extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findByOrganizerIdOrderByIdDesc(Long organizerId);

    List<Coupon> findByEventId(Long eventId);
}