package com.ticketflow.backend.service;

import com.ticketflow.backend.entity.ActivityLog;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.ActivityLogRepository;
import com.ticketflow.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository, UserRepository userRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
    }

    public void logActivity(User user, String action, String details) {
        try {
            ActivityLog log = new ActivityLog(user, action, details);
            activityLogRepository.save(log);
        } catch (Exception e) {
            // Logging failure should not break primary operation
            System.err.println("Failed to save activity log: " + e.getMessage());
        }
    }

    public void log(Long userId, String action, String details) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }
            logActivity(user, action, details);
        } catch (Exception e) {
            System.err.println("Failed to log activity by userId: " + e.getMessage());
        }
    }

    public List<ActivityLog> getRecentLogs() {
        return activityLogRepository.findTop50ByOrderByTimestampDesc();
    }
}
