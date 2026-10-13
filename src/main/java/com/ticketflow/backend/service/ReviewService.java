package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.ReviewRequest;
import com.ticketflow.backend.dto.ReviewResponse;
import com.ticketflow.backend.entity.Event;
import com.ticketflow.backend.entity.Review;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.EventRepository;
import com.ticketflow.backend.repository.ReviewRepository;
import com.ticketflow.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;

    public ReviewService(
            ReviewRepository reviewRepository,
            EventRepository eventRepository,
            UserRepository userRepository,
            ActivityLogService activityLogService) {
        this.reviewRepository = reviewRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
    }

    public ReviewResponse addReview(User customer, ReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5 stars");
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // If review already exists by this customer for this event, update it
        Review review = reviewRepository.findByEventAndCustomer(event, customer)
                .orElse(new Review(event, customer, request.getRating(), request.getComment()));

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review saved = reviewRepository.save(review);
        activityLogService.logActivity(customer, "EVENT_REVIEWED",
                "Customer reviewed event '" + event.getTitle() + "' with " + request.getRating() + " stars");

        return toResponse(saved);
    }

    public List<ReviewResponse> getReviewsForEvent(Long eventId) {
        List<Review> reviews = reviewRepository.findByEventIdOrderByCreatedAtDesc(eventId);
        return reviews.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Map<String, Object> getEventRatingSummary(Long eventId) {
        Double avg = reviewRepository.getAverageRatingForEvent(eventId);
        Long count = reviewRepository.getReviewCountForEvent(eventId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        summary.put("reviewCount", count != null ? count : 0L);
        return summary;
    }

    private ReviewResponse toResponse(Review review) {
        String customerName = "Attendee";
        if (review.getCustomer() != null) {
            String first = review.getCustomer().getFirstName();
            String last = review.getCustomer().getLastName();
            customerName = (first != null ? first : "") + (last != null ? " " + last : "");
            if (customerName.trim().isEmpty()) {
                customerName = review.getCustomer().getEmail();
            }
        }

        return new ReviewResponse(
                review.getId(),
                review.getEvent().getId(),
                review.getEvent().getTitle(),
                review.getCustomer().getId(),
                customerName.trim(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}
