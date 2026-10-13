package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.LoginRequest;
import com.ticketflow.backend.dto.RegisterRequest;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Optional;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        String role = request.getRole();

        if (role == null ||
            (!role.equals("CUSTOMER")
            && !role.equals("ORGANIZER")
            && !role.equals("STALL_OWNER"))) {

            role = "CUSTOMER";
        }

        User user = new User();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        user.setPassword(
            passwordEncoder.encode(request.getPassword())
        );

        user.setRole(role);

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                    new RuntimeException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }
    
    public User updateProfile(Long id, Map<String, String> request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                    new RuntimeException("User not found")
                );

        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String email = request.get("email");
        String phone = request.get("phone");

        if (firstName == null || firstName.trim().isEmpty()
                || lastName == null || lastName.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException(
                "All profile fields are required"
            );
        }

        email = email.trim();

        Optional<User> existingUser =
                userRepository.findByEmail(email);

        if (existingUser.isPresent()
                && !existingUser.get().getId().equals(id)) {
            throw new RuntimeException(
                "Email is already registered to another user"
            );
        }

        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(email);
        user.setPhone(phone.trim());

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");
        boolean passwordChanged = false;

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                throw new RuntimeException("Current password is required to change password");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            if (newPassword.trim().length() < 6) {
                throw new RuntimeException("New password must be at least 6 characters");
            }
            user.setPassword(passwordEncoder.encode(newPassword.trim()));
            passwordChanged = true;
        }

        User savedUser = userRepository.save(user);

        try {
            if (passwordChanged) {
                activityLogService.logActivity(savedUser, "PASSWORD_CHANGED", "User changed account password");
            } else {
                activityLogService.logActivity(savedUser, "PROFILE_UPDATED", "User updated profile details");
            }
        } catch (Exception ignored) {
        }

        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(Long id, String role) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (role == null || role.trim().isEmpty()) {
            throw new RuntimeException("Role cannot be empty");
        }
        user.setRole(role.trim().toUpperCase());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
}