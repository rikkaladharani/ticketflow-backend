package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.StallItemRequest;
import com.ticketflow.backend.entity.Stall;
import com.ticketflow.backend.entity.StallItem;
import com.ticketflow.backend.repository.StallItemRepository;
import com.ticketflow.backend.repository.StallRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StallItemService {

    private final StallItemRepository stallItemRepository;
    private final StallRepository stallRepository;

    public StallItemService(
            StallItemRepository stallItemRepository,
            StallRepository stallRepository) {

        this.stallItemRepository = stallItemRepository;
        this.stallRepository = stallRepository;
    }

    public StallItem createItem(StallItemRequest request) {

        Stall stall = stallRepository.findById(request.getStallId())
                .orElseThrow(() -> new RuntimeException("Stall not found"));

        if (!"APPROVED".equals(stall.getStatus())
                && !"ACTIVE".equals(stall.getStatus())) {

            throw new RuntimeException(
                    "Stall must be approved before adding items"
            );
        }

        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than zero");
        }

        if (request.getAvailableQuantity() == null
                || request.getAvailableQuantity() < 0) {

            throw new RuntimeException(
                    "Available quantity cannot be negative"
            );
        }

        StallItem item = new StallItem();

        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setPrice(request.getPrice());
        item.setAvailableQuantity(request.getAvailableQuantity());
        item.setAvailable(request.getAvailableQuantity() > 0);
        item.setStall(stall);

        return stallItemRepository.save(item);
    }

    public List<StallItem> getAllItems() {
        return stallItemRepository.findAll();
    }

    public StallItem getItemById(Long id) {

        return stallItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public List<StallItem> getItemsByStall(Long stallId) {

        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found"));

        return stallItemRepository.findByStall(stall);
    }

    public List<StallItem> getAvailableItemsByStall(Long stallId) {

        Stall stall = stallRepository.findById(stallId)
                .orElseThrow(() -> new RuntimeException("Stall not found"));

        return stallItemRepository.findByStallAndAvailableTrue(stall);
    }
}