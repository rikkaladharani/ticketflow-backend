package com.ticketflow.backend.repository;

import com.ticketflow.backend.entity.Stall;
import com.ticketflow.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StallRepository extends JpaRepository<Stall, Long> {

    List<Stall> findByOwner(User owner);

    List<Stall> findByStatus(String status);
}