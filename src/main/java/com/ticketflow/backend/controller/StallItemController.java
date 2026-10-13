package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.StallItemRequest;
import com.ticketflow.backend.entity.StallItem;
import com.ticketflow.backend.service.StallItemService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stall-items")
@CrossOrigin
public class StallItemController {

    private final StallItemService stallItemService;

    public StallItemController(StallItemService stallItemService) {
        this.stallItemService = stallItemService;
    }

    @PostMapping
    public ResponseEntity<StallItem> createItem(
            @RequestBody StallItemRequest request) {

        return ResponseEntity.ok(
                stallItemService.createItem(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<StallItem>> getAllItems() {

        return ResponseEntity.ok(
                stallItemService.getAllItems()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<StallItem> getItemById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                stallItemService.getItemById(id)
        );
    }

    @GetMapping("/stall/{stallId}")
    public ResponseEntity<List<StallItem>> getItemsByStall(
            @PathVariable Long stallId) {

        return ResponseEntity.ok(
                stallItemService.getItemsByStall(stallId)
        );
    }

    @GetMapping("/stall/{stallId}/available")
    public ResponseEntity<List<StallItem>> getAvailableItemsByStall(
            @PathVariable Long stallId) {

        return ResponseEntity.ok(
                stallItemService.getAvailableItemsByStall(stallId)
        );
    }
}