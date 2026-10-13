package com.ticketflow.backend.config;

import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed standard project demo accounts
        seedOrUpdateUser("admin@ticketflow.com", "admin123", "ADMIN", "System", "Admin", "9876543210");
        seedOrUpdateUser("organizer@ticketflow.com", "organizer123", "ORGANIZER", "Event", "Organizer", "9876543211");
        seedOrUpdateUser("customer@ticketflow.com", "customer123", "CUSTOMER", "Jane", "Customer", "9876543212");
        seedOrUpdateUser("stallowner@ticketflow.com", "stallowner123", "STALL_OWNER", "Bob", "StallOwner", "9876543213");

        // Also sync passwords on legacy existing test accounts in database
        updatePasswordIfPresent("admin@gmail.com", "admin123");
        updatePasswordIfPresent("organizer@test.com", "organizer123");
        updatePasswordIfPresent("test@example.com", "customer123");
        updatePasswordIfPresent("stall@test.com", "stallowner123");
    }

    private void seedOrUpdateUser(String email, String plainPassword, String role, String firstName, String lastName, String phone) {
        Optional<User> existing = userRepository.findByEmail(email);
        User user = existing.orElseGet(User::new);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setRole(role);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        userRepository.save(user);
    }

    private void updatePasswordIfPresent(String email, String plainPassword) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(plainPassword));
            userRepository.save(user);
        });
    }
}
