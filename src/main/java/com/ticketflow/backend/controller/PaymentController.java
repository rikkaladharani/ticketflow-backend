package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.PaymentRequest;
import com.ticketflow.backend.entity.Payment;
import com.ticketflow.backend.service.PaymentService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174"})
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> processPayment(
            @RequestBody PaymentRequest request) {

        try {

            Payment payment =
                    paymentService.processPayment(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(payment);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    public record ErrorResponse(String message) {
    }
}