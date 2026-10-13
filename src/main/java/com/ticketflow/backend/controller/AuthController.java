package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.LoginRequest;
import com.ticketflow.backend.dto.RegisterRequest;
import com.ticketflow.backend.entity.User;
import com.ticketflow.backend.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.List;
import com.ticketflow.backend.service.JwtService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class AuthController {

    private final AuthService authService;
private final JwtService jwtService;

    public AuthController(
        AuthService authService,
        JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
}

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        try {
            User user = authService.register(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new RegisterResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getRole()
                )
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    
@PostMapping("/login")
public ResponseEntity<?> login(
        @RequestBody LoginRequest request) {

    try {
        User user = authService.login(request);

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return ResponseEntity.ok(
            new LoginResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                token
            )
        );

    } catch (RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(e.getMessage()));
    }
}


    
    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        try {
            User user = authService.updateProfile(id, request);

            return ResponseEntity.ok(
                new ProfileResponse(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getRole()
                )
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    public record ProfileResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String phone,
            String role
    ) {}


    public record RegisterResponse(
            Long id,
            String firstName,
            String lastName,
            String email,
            String role
    ) {}

    public record LoginResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String role,
        String token
) {} {}

    public record ErrorResponse(
            String message
    ) {}
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = authService.getAllUsers();

            return ResponseEntity.ok(
                users.stream()
                    .map(user -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", user.getId());
                        map.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
                        map.put("lastName", user.getLastName() != null ? user.getLastName() : "");
                        map.put("email", user.getEmail() != null ? user.getEmail() : "");
                        map.put("phone", user.getPhone() != null ? user.getPhone() : "");
                        map.put("role", user.getRole() != null ? user.getRole() : "CUSTOMER");
                        return map;
                    })
                    .toList()
            );

        } catch (RuntimeException e) {
            return ResponseEntity
                    .internalServerError()
                    .body(new ErrorResponse("Could not retrieve users: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            User updated = authService.updateUserRole(id, role);
            return ResponseEntity.ok(Map.of(
                    "id", updated.getId(),
                    "email", updated.getEmail(),
                    "role", updated.getRole()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            authService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
}