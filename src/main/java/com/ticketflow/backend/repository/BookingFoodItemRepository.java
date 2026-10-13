package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Booking;
import com.ticketflow.backend.entity.BookingFoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingFoodItemRepository extends JpaRepository<BookingFoodItem, Long> {

    List<BookingFoodItem> findByBooking(Booking booking);
}