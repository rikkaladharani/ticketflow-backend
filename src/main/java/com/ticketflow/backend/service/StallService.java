package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.StallRequest;
import com.ticketflow.backend.entity.Stall;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.StallRepository;
import com.ticketflow.backend.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StallService {

    private final StallRepository stallRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    public StallService(
            StallRepository stallRepository,
            UserRepository userRepository,
            NotificationService notificationService,
            ActivityLogService activityLogService) {

        this.stallRepository = stallRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.activityLogService = activityLogService;
    }

    public Stall createStall(StallRequest request) {

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        if (!"STALL_OWNER".equals(owner.getRole())) {
            throw new RuntimeException("User is not a stall owner");
        }

        Stall stall = new Stall();

        stall.setStallName(request.getStallName());
        stall.setDescription(request.getDescription());
        stall.setLocation(request.getLocation());
        stall.setStatus("PENDING");
        stall.setOwner(owner);

        Stall saved = stallRepository.save(stall);
        activityLogService.log(owner.getId(), "STALL_SUBMITTED", "Submitted stall for admin approval: " + stall.getStallName());
        return saved;
    }

    public List<Stall> getAllStalls() {
        return stallRepository.findAll();
    }

    public Stall getStallById(Long id) {

        return stallRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stall not found"));
    }

    public List<Stall> getStallsByOwner(Long ownerId) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        return stallRepository.findByOwner(owner);
    }

    public List<Stall> getStallsByStatus(String status) {
        return stallRepository.findByStatus(status);
    }

    public Stall approveStall(Long stallId) {

        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found"));

        stall.setStatus("APPROVED");
        Stall saved = stallRepository.save(stall);

        if (stall.getOwner() != null) {
            notificationService.createNotification(
                    stall.getOwner().getId(),
                    "STALL_ADMIN_APPROVED",
                    "Stall Approved by Admin",
                    "Your stall '" + stall.getStallName() + "' has been approved by admin! You can now add menu items and apply to upcoming events."
            );
            activityLogService.log(stall.getOwner().getId(), "STALL_ADMIN_APPROVED", "Admin approved stall: " + stall.getStallName());
        }

        return saved;
    }

    public Stall rejectStall(Long stallId) {

        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found"));

        stall.setStatus("REJECTED");
        Stall saved = stallRepository.save(stall);

        if (stall.getOwner() != null) {
            notificationService.createNotification(
                    stall.getOwner().getId(),
                    "STALL_ADMIN_REJECTED",
                    "Stall Registration Rejected",
                    "Your stall registration for '" + stall.getStallName() + "' was reviewed and rejected by admin."
            );
            activityLogService.log(stall.getOwner().getId(), "STALL_ADMIN_REJECTED", "Admin rejected stall: " + stall.getStallName());
        }

        return saved;
    }
}