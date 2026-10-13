package com.ticketflow.backend.dto;

import java.util.List;

public class AiChatRequest {

    private String message;
    private List<ChatMessageItem> history;

    public AiChatRequest() {
    }

    public AiChatRequest(String message, List<ChatMessageItem> history) {
        this.message = message;
        this.history = history;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ChatMessageItem> getHistory() {
        return history;
    }

    public void setHistory(List<ChatMessageItem> history) {
        this.history = history;
    }

    public static class ChatMessageItem {
        private String role; // "user" or "assistant"
        private String content;

        public ChatMessageItem() {
        }

        public ChatMessageItem(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
