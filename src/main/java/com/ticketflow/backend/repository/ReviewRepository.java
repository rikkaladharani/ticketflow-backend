package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.Review;
import com.ticketflow.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByEventOrderByCreatedAtDesc(Event event);

    List<Review> findByEventIdOrderByCreatedAtDesc(Long eventId);

    Optional<Review> findByEventAndCustomer(Event event, User customer);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.event.id = :eventId")
    Double getAverageRatingForEvent(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.event.id = :eventId")
    Long getReviewCountForEvent(@Param("eventId") Long eventId);
}
