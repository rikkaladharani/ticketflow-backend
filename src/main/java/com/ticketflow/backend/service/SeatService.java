package com.ticketflow.backend.service;

import com.ticketflow.backend.dto.SeatGenerationRequest;
import com.ticketflow.backend.entity.Seat;
import com.ticketflow.backend.entity.VenueSection;
import com.ticketflow.backend.repository.SeatRepository;
import com.ticketflow.backend.repository.VenueSectionRepository;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final VenueSectionRepository venueSectionRepository;

    public SeatService(
            SeatRepository seatRepository,
            VenueSectionRepository venueSectionRepository) {

        this.seatRepository = seatRepository;
        this.venueSectionRepository = venueSectionRepository;
    }

    public List<Seat> generateSeats(
            SeatGenerationRequest request) {

        VenueSection section =
                venueSectionRepository.findById(request.getSectionId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Venue section not found"));

        long existingSeats =
                seatRepository.countBySection(section);

        if (existingSeats > 0) {
            throw new RuntimeException(
                    "Seats already generated for this section");
        }

        List<Seat> seats = new ArrayList<>();

        for (int row = 0; row < section.getRowCount(); row++) {

            String rowName = generateRowName(row);

            for (int number = 1;
                 number <= section.getSeatsPerRow();
                 number++) {

                Seat seat = new Seat();

                seat.setSeatRow(rowName);
                seat.setSeatNumber(number);

                seat.setSeatType(
                        section.getSectionType()
                );

                seat.setPrice(request.getPrice());

                seat.setStatus("AVAILABLE");

                seat.setSection(section);

                seats.add(seat);
            }
        }

        return seatRepository.saveAll(seats);
    }

    public List<Seat> getAllSeats() {
        return seatRepository.findAll();
    }

    public List<Seat> getSeatsBySection(Long sectionId) {

        VenueSection section =
                venueSectionRepository.findById(sectionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Venue section not found"));

        return seatRepository.findBySection(section);
    }

    public List<Seat> getAvailableSeats(Long sectionId) {

        VenueSection section =
                venueSectionRepository.findById(sectionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Venue section not found"));

        return seatRepository.findBySectionAndStatus(
                section,
                "AVAILABLE"
        );
    }

    private String generateRowName(int rowIndex) {

        StringBuilder result = new StringBuilder();

        int number = rowIndex + 1;

        while (number > 0) {

            int remainder = (number - 1) % 26;

            result.insert(
                    0,
                    (char) ('A' + remainder)
            );

            number = (number - 1) / 26;
        }

        return result.toString();
    }
}