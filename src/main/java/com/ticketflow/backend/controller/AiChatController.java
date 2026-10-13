package com.ticketflow.backend.controller;

import com.ticketflow.backend.dto.AiChatRequest;
import com.ticketflow.backend.dto.AiChatResponse;
import com.ticketflow.backend.service.AiChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AiChatController {

    private final AiChatService aiChatService;

    public AiChatController(AiChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        AiChatResponse response = aiChatService.processMessage(request);
        return ResponseEntity.ok(response);
    }
}
