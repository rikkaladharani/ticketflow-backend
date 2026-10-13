package com.ticketflow.backend.dto;

import java.util.List;

public class AiChatResponse {

    private String response;
    private List<String> sources;
    private boolean isRagAugmented;

    public AiChatResponse() {
    }

    public AiChatResponse(String response, List<String> sources, boolean isRagAugmented) {
        this.response = response;
        this.sources = sources;
        this.isRagAugmented = isRagAugmented;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public boolean isRagAugmented() {
        return isRagAugmented;
    }

    public void setRagAugmented(boolean ragAugmented) {
        isRagAugmented = ragAugmented;
    }
}
