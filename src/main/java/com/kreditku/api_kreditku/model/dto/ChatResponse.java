package com.kreditku.api_kreditku.model.dto;

public class ChatResponse {
    private String recommendation;
    private String conversationId;
    private String provider;
    private int remainingMessages;
    private boolean success;
    private String error;

    // Success constructor
    public ChatResponse(String recommendation, String conversationId, String provider, int remainingMessages) {
        this.recommendation = recommendation;
        this.conversationId = conversationId;
        this.provider = provider;
        this.remainingMessages = remainingMessages;
        this.success = true;
    }

    public ChatResponse(String error) {
        this.error = error;
        this.success = false;
    }

    // Getters and Setters
    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getRemainingMessages() {
        return remainingMessages;
    }

    public void setRemainingMessages(int remainingMessages) {
        this.remainingMessages = remainingMessages;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
