package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Stall;
import com.ticketflow.backend.entity.StallItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StallItemRepository extends JpaRepository<StallItem, Long> {

    List<StallItem> findByStall(Stall stall);

    List<StallItem> findByStallAndAvailableTrue(Stall stall);
}