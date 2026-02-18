package com.kreditku.api_kreditku.model;

public class RecommendationResponse {
    private String recommendation;
    private boolean success;
    private String error;
    private String provider;

    public RecommendationResponse(String recommendation, String provider) {
        this.recommendation = recommendation;
        this.success = true;
        this.provider = provider;
    }

    public RecommendationResponse(String error, boolean success) {
        this.error = error;
        this.success = success;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
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

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
