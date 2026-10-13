package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.StallRequest;
import com.ticketflow.backend.entity.Stall;
import com.ticketflow.backend.service.StallService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stalls")
@CrossOrigin
public class StallController {

    private final StallService stallService;

    public StallController(StallService stallService) {
        this.stallService = stallService;
    }

    @PostMapping
    public ResponseEntity<Stall> createStall(
            @RequestBody StallRequest request) {

        return ResponseEntity.ok(
                stallService.createStall(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<Stall>> getAllStalls() {

        return ResponseEntity.ok(
                stallService.getAllStalls()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stall> getStallById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                stallService.getStallById(id)
        );
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Stall>> getStallsByOwner(
            @PathVariable Long ownerId) {

        return ResponseEntity.ok(
                stallService.getStallsByOwner(ownerId)
        );
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Stall>> getStallsByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                stallService.getStallsByStatus(status)
        );
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<Stall> approveStall(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                stallService.approveStall(id)
        );
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<Stall> rejectStall(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                stallService.rejectStall(id)
        );
    }
}