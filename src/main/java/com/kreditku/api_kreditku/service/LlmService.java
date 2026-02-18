package com.kreditku.api_kreditku.service;

public interface LlmService {
    String getRecommendation(String expensesText);

    String chat(String userMessage);

    boolean isHealthy();

    String getProviderName();
}
